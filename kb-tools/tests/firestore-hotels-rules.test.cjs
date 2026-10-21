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
const { doc, getDoc, setDoc, updateDoc, deleteDoc } = require("firebase/firestore");

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
    const hotelRef = doc(admin, "hotels", "hotel-istanbul-demo");

    // `isAdmin()` depends on the server-maintained admins/{uid} document.
    await env.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), "admins", "admin-1"), { role: "admin" });
    });

    await assertFails(getDoc(doc(anonymous, "hotels", "hotel-istanbul-demo")));
    await assertFails(setDoc(doc(applicant, "hotels", "hotel-istanbul-demo"), hotel()));

    await assertSucceeds(setDoc(hotelRef, hotel()));
    await assertSucceeds(getDoc(doc(applicant, "hotels", "hotel-istanbul-demo")));
    await assertFails(setDoc(doc(admin, "hotels", "bad-country"), hotel({ countryCode: "DE" })));
    await assertFails(setDoc(doc(admin, "hotels", "bad-lat"), hotel({ lat: 91 })));
    await assertFails(updateDoc(doc(applicant, "hotels", "hotel-istanbul-demo"), { name: "No access" }));
    await assertSucceeds(updateDoc(hotelRef, { name: "Updated Demo Hotel", updatedAt: 1792627200001 }));
    await assertSucceeds(deleteDoc(hotelRef));

    console.log("FIRESTORE EMULATOR: hotels rules passed — signed-in read; admin-only validated CRUD.");
  } finally {
    await env.cleanup();
  }
})().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
