import * as admin from "firebase-admin";
import { HttpsError, onCall } from "firebase-functions/v2/https";

const db = admin.firestore();
const INTERNAL_DOMAIN = "users.karbama.invalid";
const USERNAME = /^[a-z][a-z0-9._-]{2,31}$/;
type Role = "applicant" | "collaborator";
type Caller = { auth?: { uid: string } | null };

function callerUid(request: Caller): string { const uid = request.auth?.uid; if (!uid) throw new HttpsError("unauthenticated", "Sign in is required."); return uid; }
function usernameFrom(value: unknown): string { if (typeof value !== "string") throw new HttpsError("invalid-argument", "A username is required."); const username = value.trim().toLowerCase(); if (!USERNAME.test(username)) throw new HttpsError("invalid-argument", "Username must be 3–32 lowercase English characters."); return username; }
function passwordFrom(value: unknown): string { if (typeof value !== "string" || value.length < 8) throw new HttpsError("invalid-argument", "Password must be at least 8 characters."); return value; }
function emailFor(username: string): string { return `${username}@${INTERNAL_DOMAIN}`; }
async function requireMainAdmin(uid: string): Promise<void> { if (!(await db.collection("admins").doc(uid).get()).exists) throw new HttpsError("permission-denied", "Main manager access is required."); }
async function requireActiveCollaborator(uid: string): Promise<void> {
  const [authUser, collaborator] = await Promise.all([admin.auth().getUser(uid), db.collection("collaborators").doc(uid).get()]);
  if (authUser.customClaims?.karbamaRole !== "collaborator" || !collaborator.exists || collaborator.get("active") !== true) throw new HttpsError("permission-denied", "An active collaborator account is required.");
}
async function createIdentity(input: { username: string; password: string; role: Role; createdBy: string }): Promise<{ uid: string; username: string }> {
  const usernameRef = db.collection("usernames").doc(input.username);
  if ((await usernameRef.get()).exists) throw new HttpsError("already-exists", "This username is already in use.");
  let user: admin.auth.UserRecord;
  try { user = await admin.auth().createUser({ email: emailFor(input.username), password: input.password, displayName: input.username, disabled: false }); }
  catch (error: unknown) { if ((error as { code?: string })?.code === "auth/email-already-exists") throw new HttpsError("already-exists", "This username is already in use."); throw new HttpsError("internal", "Could not create the identity."); }
  try {
    await admin.auth().setCustomUserClaims(user.uid, { karbamaRole: input.role });
    const now = admin.firestore.FieldValue.serverTimestamp();
    const batch = db.batch();
    batch.create(usernameRef, { uid: user.uid, createdAt: now });
    batch.create(db.collection("accounts").doc(user.uid), { username: input.username, role: input.role, createdBy: input.createdBy, active: true, createdAt: now });
    if (input.role === "collaborator") batch.create(db.collection("collaborators").doc(user.uid), { username: input.username, createdBy: input.createdBy, active: true, createdAt: now });
    await batch.commit(); return { uid: user.uid, username: input.username };
  } catch (error) { await admin.auth().deleteUser(user.uid).catch(() => undefined); if (error instanceof HttpsError) throw error; throw new HttpsError("internal", "Could not store account metadata."); }
}
export const provisionCollaboratorIdentity = onCall({ region: "europe-west1" }, async (request) => { const uid = callerUid(request); await requireMainAdmin(uid); return createIdentity({ username: usernameFrom(request.data?.username), password: passwordFrom(request.data?.password), role: "collaborator", createdBy: uid }); });
export const provisionApplicantIdentity = onCall({ region: "europe-west1" }, async (request) => { const uid = callerUid(request); await requireActiveCollaborator(uid); return createIdentity({ username: usernameFrom(request.data?.username), password: passwordFrom(request.data?.password), role: "applicant", createdBy: uid }); });
export const setCollaboratorActive = onCall({ region: "europe-west1" }, async (request) => { const managerUid = callerUid(request); await requireMainAdmin(managerUid); const uid = typeof request.data?.uid === "string" ? request.data.uid : ""; const active = request.data?.active === true; if (!uid) throw new HttpsError("invalid-argument", "A collaborator UID is required."); const ref = db.collection("collaborators").doc(uid); if (!(await ref.get()).exists) throw new HttpsError("not-found", "Collaborator not found."); await Promise.all([ref.update({ active, updatedAt: admin.firestore.FieldValue.serverTimestamp() }), db.collection("accounts").doc(uid).update({ active, updatedAt: admin.firestore.FieldValue.serverTimestamp() })]); return { uid, active }; });
