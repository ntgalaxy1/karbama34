import * as admin from "firebase-admin";
import { HttpsError, onCall } from "firebase-functions/v2/https";

const USERNAME = /^[a-z][a-z0-9._-]{2,31}$/;

// This accessor intentionally runs only inside callable handlers. index.ts owns
// initializeApp(), and ES module imports are evaluated before its top-level code.
const db = () => admin.firestore();

type AccountRole = "applicant" | "collaborator";
type ProvisionInput = { username: string; password: string };

function authenticatedUid(request: { auth?: { uid: string } | null }): string {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Sign in required.");
  return request.auth.uid;
}

function readInput(data: unknown): ProvisionInput {
  const raw = data as { username?: unknown; password?: unknown } | null;
  const username = String(raw?.username ?? "").trim().toLowerCase();
  const password = raw?.password;
  if (!USERNAME.test(username) || typeof password !== "string" || password.length < 8) {
    throw new HttpsError("invalid-argument", "Invalid username or password.");
  }
  return { username, password };
}

async function managerRequired(uid: string): Promise<void> {
  if (!(await db().collection("admins").doc(uid).get()).exists) {
    throw new HttpsError("permission-denied", "Main manager required.");
  }
}

async function activeCollaboratorRequired(
  request: { auth?: { uid: string; token?: Record<string, unknown> } | null }
): Promise<string> {
  const uid = authenticatedUid(request);
  if (request.auth?.token?.karbamaRole !== "collaborator") {
    throw new HttpsError("permission-denied", "Collaborator role required.");
  }
  const collaborator = await db().collection("collaborators").doc(uid).get();
  if (!collaborator.exists || collaborator.get("active") !== true) {
    throw new HttpsError("permission-denied", "Active collaborator required.");
  }
  return uid;
}

async function createIdentity(
  role: AccountRole,
  createdBy: string,
  input: ProvisionInput
): Promise<{ uid: string; username: string }> {
  let user: admin.auth.UserRecord;
  try {
    user = await admin.auth().createUser({
      // Firebase Auth requires an email for this provider. This internal mapping
      // is never requested from a user and never copied into Firestore profiles.
      email: `${input.username}@users.karbama.invalid`,
      password: input.password,
    });
  } catch (error: unknown) {
    if ((error as { code?: string }).code === "auth/email-already-exists") {
      throw new HttpsError("already-exists", "Username is already in use.");
    }
    throw error;
  }

  try {
    await admin.auth().setCustomUserClaims(user.uid, { karbamaRole: role });
    const batch = db().batch();
    batch.set(db().collection("accounts").doc(user.uid), {
      username: input.username,
      role,
      createdBy,
      active: true,
    });
    if (role === "collaborator") {
      batch.set(db().collection("collaborators").doc(user.uid), {
        username: input.username,
        createdBy,
        active: true,
      });
    }
    await batch.commit();
  } catch (error) {
    // Do not leave a usable account when authoritative metadata could not be written.
    await admin.auth().deleteUser(user.uid).catch(() => undefined);
    throw error;
  }

  return { uid: user.uid, username: input.username };
}

/** Only the main manager can create collaborator identities. */
export const provisionCollaboratorIdentity = onCall({ region: "europe-west1" }, async (request) => {
  const managerUid = authenticatedUid(request);
  await managerRequired(managerUid);
  return createIdentity("collaborator", managerUid, readInput(request.data));
});

/** An active collaborator can create only a jobseeker identity attributed to themselves. */
export const provisionApplicantIdentity = onCall({ region: "europe-west1" }, async (request) => {
  const collaboratorUid = await activeCollaboratorRequired(request);
  return createIdentity("applicant", collaboratorUid, readInput(request.data));
});

/** Main manager can suspend/re-enable a collaborator; clients never write role metadata. */
export const setCollaboratorActive = onCall({ region: "europe-west1" }, async (request) => {
  const managerUid = authenticatedUid(request);
  await managerRequired(managerUid);
  const data = request.data as { uid?: unknown; active?: unknown } | null;
  const uid = String(data?.uid ?? "").trim();
  const active = data?.active;
  if (!uid || typeof active !== "boolean") {
    throw new HttpsError("invalid-argument", "uid and active are required.");
  }
  const collaborator = db().collection("collaborators").doc(uid);
  if (!(await collaborator.get()).exists) {
    throw new HttpsError("not-found", "Collaborator not found.");
  }
  await Promise.all([
    collaborator.update({ active }),
    db().collection("accounts").doc(uid).update({ active }),
  ]);
  return { uid, active };
});
