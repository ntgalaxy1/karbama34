/*
 * Karbama v38 payment boundary — Firebase Functions Gen 2.
 *
 * Deliberately safe by default: merchant credentials and verification endpoints are
 * not available yet, so no callable or callback can mark a payment as paid. The
 * shape below is the server-only contract to complete after merchant onboarding.
 */
import * as admin from "firebase-admin";
import { defineSecret, defineString } from "firebase-functions/params";
import { HttpsError, onCall, onRequest } from "firebase-functions/v2/https";
import { logger } from "firebase-functions";

export { provisionCollaboratorIdentity, provisionApplicantIdentity, setCollaboratorActive } from "./identity";

admin.initializeApp();
const db = admin.firestore();

// Credentials are secret-bound to each function. Never mirror any of these to Android.
const NAVASAN_API_KEY = defineSecret("NAVASAN_API_KEY");
const ZARINPAL_MERCHANT_ID = defineSecret("ZARINPAL_MERCHANT_ID");
const IYZICO_API_KEY = defineSecret("IYZICO_API_KEY");
const IYZICO_SECRET_KEY = defineSecret("IYZICO_SECRET_KEY");

// Non-secret safety latch. It is intentionally false until merchant account, callback and
// provider verification tests exist. A secret is not used for this because it is not a credential.
const PAYMENTS_ENABLED = defineString("PAYMENTS_ENABLED", { default: "false" });

const TRY_AMOUNT = 500;
const QUOTE_TTL_MS = 15 * 60 * 1000;

type Method = "zarinpal" | "iyzico";
type PaymentSession = {
  uid: string;
  method: Method;
  tryAmount: number;
  irrAmount: number | null;
  rateItem: "try_hav" | null;
  rateIrrPerTry: number | null;
  createdAt: admin.firestore.Timestamp;
  expiresAt: admin.firestore.Timestamp;
  providerStatus: "quoted" | "setup_required" | "created" | "verified" | "failed";
  providerToken: string | null;
};

function uidOf(request: { auth?: { uid: string } | null }): string {
  if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Sign in is required.");
  return request.auth.uid;
}

function needSetup(name: string): never {
  throw new HttpsError(
    "failed-precondition",
    `${name} is not live. Complete merchant onboarding, secret binding, callback registration and server-side verification first.`
  );
}

function liveAllowed(): boolean {
  return PAYMENTS_ENABLED.value().trim().toLowerCase() === "true";
}

function parseNavasanTryHav(payload: unknown): number {
  // Navasan returns a record for try_hav. Its exact field/unit must be confirmed against
  // the subscribed API response before the safety latch can be enabled.
  const row = (payload as { try_hav?: { value?: string | number } })?.try_hav;
  const raw = Number(String(row?.value ?? "").replace(/,/g, ""));
  if (!Number.isFinite(raw) || raw <= 0) throw new HttpsError("unavailable", "try_hav is unavailable.");
  return raw;
}

/**
 * Reads try_hav only on the server and locks the resulting IRR amount for 15 minutes.
 * This function stays unavailable until a Navasan key and unit verification are completed.
 */
export const createIranQuote = onCall(
  { region: "europe-west1", secrets: [NAVASAN_API_KEY] },
  async (request) => {
    const uid = uidOf(request);
    if (!liveAllowed()) needSetup("Iran payment quote");

    const apiKey = NAVASAN_API_KEY.value();
    if (!apiKey) needSetup("Navasan API key");

    // Provider URL and unit conversion are deliberately kept server-side. Do not copy this
    // key or a rate to the client. Replace this guarded fetch with the verified subscribed
    // Navasan endpoint only during the live merchant integration.
    const response = await fetch(`https://api.navasan.tech/latest/?api_key=${encodeURIComponent(apiKey)}`);
    if (!response.ok) throw new HttpsError("unavailable", "Exchange-rate provider is unavailable.");
    const rate = parseNavasanTryHav(await response.json());
    const now = Date.now();
    const doc = db.collection("paymentSessions").doc();
    const session: PaymentSession = {
      uid,
      method: "zarinpal",
      tryAmount: TRY_AMOUNT,
      irrAmount: Math.ceil(rate * TRY_AMOUNT),
      rateItem: "try_hav",
      rateIrrPerTry: rate,
      createdAt: admin.firestore.Timestamp.fromMillis(now),
      expiresAt: admin.firestore.Timestamp.fromMillis(now + QUOTE_TTL_MS),
      providerStatus: "quoted",
      providerToken: null
    };
    await doc.set(session);
    return { sessionId: doc.id, tryAmount: TRY_AMOUNT, irrAmount: session.irrAmount, expiresAt: now + QUOTE_TTL_MS };
  }
);

async function readFreshSession(uid: string, sessionId: unknown, method: Method) {
  if (typeof sessionId !== "string" || !sessionId) throw new HttpsError("invalid-argument", "A payment session is required.");
  const snap = await db.collection("paymentSessions").doc(sessionId).get();
  const session = snap.data() as PaymentSession | undefined;
  if (!session || session.uid !== uid || session.method !== method) throw new HttpsError("permission-denied", "Invalid payment session.");
  if (session.expiresAt.toMillis() <= Date.now()) throw new HttpsError("deadline-exceeded", "Payment quote has expired.");
  return { ref: snap.ref, session };
}

/**
 * Stub boundary for Zarinpal PaymentRequest. After onboarding, it must create the provider
 * request server-side, save only provider authority/token, and return the hosted URL.
 */
export const startZarinpalPayment = onCall(
  { region: "europe-west1", secrets: [ZARINPAL_MERCHANT_ID] },
  async (request) => {
    const uid = uidOf(request);
    const { ref } = await readFreshSession(uid, request.data?.sessionId, "zarinpal");
    if (!liveAllowed() || !ZARINPAL_MERCHANT_ID.value()) needSetup("Zarinpal");
    await ref.update({ providerStatus: "setup_required" });
    needSetup("Zarinpal request/verify implementation");
  }
);

/**
 * Stub boundary for iyzico hosted Checkout Form. tryAmount is server-fixed at ₺500; no
 * amount supplied by the Android client is trusted.
 */
export const startIyzicoPayment = onCall(
  { region: "europe-west1", secrets: [IYZICO_API_KEY, IYZICO_SECRET_KEY] },
  async (request) => {
    const uid = uidOf(request);
    if (!liveAllowed() || !IYZICO_API_KEY.value() || !IYZICO_SECRET_KEY.value()) needSetup("iyzico");
    const now = Date.now();
    const ref = db.collection("paymentSessions").doc();
    const session: PaymentSession = {
      uid,
      method: "iyzico",
      tryAmount: TRY_AMOUNT,
      irrAmount: null,
      rateItem: null,
      rateIrrPerTry: null,
      createdAt: admin.firestore.Timestamp.fromMillis(now),
      expiresAt: admin.firestore.Timestamp.fromMillis(now + QUOTE_TTL_MS),
      providerStatus: "setup_required",
      providerToken: null
    };
    await ref.set(session);
    needSetup("iyzico Checkout Form request/retrieve implementation");
  }
);

/**
 * Callback placeholders intentionally refuse unverified redirects. Do not change either to
 * `paid` based on query parameters. Complete provider-specific API verification first.
 */
export const zarinpalCallback = onRequest(
  { region: "europe-west1", secrets: [ZARINPAL_MERCHANT_ID] },
  async (_request, response) => {
    logger.warn("Zarinpal callback received before server-side verification was enabled.");
    response.status(503).type("text/plain").send("Payment verification is not configured.");
  }
);

export const iyzicoCallback = onRequest(
  { region: "europe-west1", secrets: [IYZICO_API_KEY, IYZICO_SECRET_KEY] },
  async (_request, response) => {
    logger.warn("iyzico callback received before server-side verification was enabled.");
    response.status(503).type("text/plain").send("Payment verification is not configured.");
  }
);

/**
 * Future manager-review endpoint. The Android app currently uses Firestore manager rules for
 * the manual path. A production migration should require an admin custom claim/document,
 * confirm the receipt/reference exist, then update paymentStatus only from this trusted code.
 */
export const reviewManualPayment = onCall({ region: "europe-west1" }, async (request) => {
  uidOf(request);
  needSetup("Manual-payment server review");
});
