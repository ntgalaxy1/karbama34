/*
 * Local Firebase Emulator security test for Karbama's `hotels` collection.
 * Run through `firebase emulators:exec --only firestore` so no production
 * Firestore project, account, document, or rule is touched.
 */
const fs = require("node:fs");
const path = require("node:path");
const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { collection, deleteDoc, doc, getDoc, getDocs, query, setDoc, updateDoc, where } = require("firebase/firestore");

const projectId = "karbama-hotels-demo";
const rules = fs.readFileSync(
  path.resolve(__dirname, "../../Karbama/firestore.rules"),
  "utf8"
);
const hotel = (overrides = {}) => ({
  countryCode: "TR",
  cityId: "istanbul",
  name: "Demo Hotel",
  address: "Beyoğlu, İstanbul",
  lat: 41.0312,
  lon: 28.9741,
  ownerUid: "",
  active: true,
  updatedAt: 1792627200000,
  ...overrides,
});

(async () => {
  const env = await initializeTestEnvironment({
    projectId,
    firestore: { rules },
  });
  try {
    const anonymous = env.unauthenticatedContext().firestore();
    const applicant = env.authenticatedContext("applicant-1").firestore();
    const admin = env.authenticatedContext("admin-1").firestore();
    const collaborator = env.authenticatedContext("collab-1", { karbamaRole: "collaborator" }).firestore();
    const otherCollaborator = env.authenticatedContext("collab-2", { karbamaRole: "collaborator" }).firestore();
    const hotelRef = doc(admin, "hotels", "hotel-istanbul-demo");
    const collaboratorHotelRef = doc(collaborator, "hotels", "hotel-collab-demo");

    // `isAdmin()` depends on the server-maintained admins/{uid} document.
    await env.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), "admins", "admin-1"), { role: "admin" });
      await setDoc(doc(context.firestore(), "collaborators", "collab-1"), { active: true });
      await setDoc(doc(context.firestore(), "collaborators", "collab-2"), { active: true });
      // Callable Functions own these records in production. They are seeded only in
      // the emulator to prove the collaborator/profile rules do not trust the client.
      await setDoc(doc(context.firestore(), "accounts", "applicant-1"), {
        username: "jobseeker01", role: "applicant", createdBy: "collab-1", active: true,
      });
    });

    await assertFails(getDoc(doc(anonymous, "hotels", "hotel-istanbul-demo")));
    await assertFails(setDoc(doc(applicant, "hotels", "hotel-istanbul-demo"), hotel()));

    await assertSucceeds(setDoc(hotelRef, hotel({ ownerUid: "admin-1" })));
    await assertSucceeds(getDoc(doc(applicant, "hotels", "hotel-istanbul-demo")));
    await assertFails(getDoc(doc(collaborator, "hotels", "hotel-istanbul-demo")));
    await assertFails(setDoc(doc(admin, "hotels", "bad-country"), hotel({ ownerUid: "admin-1", countryCode: "DE" })));
    await assertFails(setDoc(doc(admin, "hotels", "bad-lat"), hotel({ ownerUid: "admin-1", lat: 91 })));
    await assertFails(updateDoc(doc(applicant, "hotels", "hotel-istanbul-demo"), { name: "No access" }));
    await assertSucceeds(updateDoc(hotelRef, { name: "Updated Demo Hotel", updatedAt: 1792627200001 }));

    // An active collaborator may only create/update/delete records stamped with their UID.
    await assertSucceeds(setDoc(collaboratorHotelRef, hotel({ ownerUid: "collab-1" })));
    await assertSucceeds(getDoc(collaboratorHotelRef));
    await assertSucceeds(getDocs(query(collection(collaborator, "hotels"), where("ownerUid", "==", "collab-1"))));
    await assertFails(getDocs(collection(collaborator, "hotels")));
    await assertFails(getDoc(doc(otherCollaborator, "hotels", "hotel-collab-demo")));
    await assertFails(setDoc(doc(collaborator, "hotels", "hotel-spoofed-owner"), hotel({ ownerUid: "admin-1" })));
    await assertSucceeds(updateDoc(collaboratorHotelRef, { name: "Collaborator Hotel", updatedAt: 1792627200002 }));
    await assertFails(updateDoc(doc(otherCollaborator, "hotels", "hotel-collab-demo"), { name: "Takeover" }));
    await assertFails(updateDoc(collaboratorHotelRef, { ownerUid: "collab-2" }));
    await assertSucceeds(deleteDoc(collaboratorHotelRef));
    await assertSucceeds(deleteDoc(hotelRef));

    // Collaborator-created applicant accounts are permanently linked through the
    // server-owned accounts/{uid} record. A different collaborator cannot read,
    // update, or take over the profile; clients cannot write account metadata.
    const applicantProfile = {
      uid: "applicant-1",
      username: "jobseeker01",
      createdBy: "collab-1",
      firstName: "Ali",
      lastName: "Rezaei",
      paymentStatus: "",
      paymentMethod: "",
      paymentReference: "",
      paymentReceiptUrl: "",
    };
    const collaboratorApplicantRef = doc(collaborator, "applicants", "applicant-1");
    await assertSucceeds(setDoc(collaboratorApplicantRef, applicantProfile));
    await assertSucceeds(getDoc(collaboratorApplicantRef));
    await assertSucceeds(getDocs(query(collection(collaborator, "applicants"), where("createdBy", "==", "collab-1"))));
    await assertFails(getDocs(collection(collaborator, "applicants")));
    await assertFails(getDoc(doc(otherCollaborator, "applicants", "applicant-1")));
    await assertSucceeds(updateDoc(collaboratorApplicantRef, { firstName: "Alireza" }));
    await assertFails(updateDoc(collaboratorApplicantRef, { createdBy: "collab-2" }));
    await assertFails(setDoc(doc(collaborator, "accounts", "spoofed"), { role: "applicant", createdBy: "collab-1" }));

    // Jobs obey the same immutable createdBy ownership boundary.
    const ownJob = doc(collaborator, "jobs", "collab-job");
    await assertSucceeds(setDoc(ownJob, { title: "Cook", createdBy: "collab-1" }));
    await assertSucceeds(getDoc(ownJob));
    await assertSucceeds(getDocs(query(collection(collaborator, "jobs"), where("createdBy", "==", "collab-1"))));
    await assertFails(getDocs(collection(collaborator, "jobs")));
    await assertFails(getDoc(doc(otherCollaborator, "jobs", "collab-job")));
    await assertFails(setDoc(doc(collaborator, "jobs", "spoofed-job"), { title: "Cook", createdBy: "admin-1" }));
    await assertFails(updateDoc(doc(otherCollaborator, "jobs", "collab-job"), { title: "Takeover" }));

    console.log("FIRESTORE EMULATOR: collaborator ownership passed — hotels, applicants, accounts, and jobs.");
  } finally {
    await env.cleanup();
  }
})().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
