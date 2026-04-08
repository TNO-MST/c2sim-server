export const C2SIM_CLAIM_KEYS = [
  "communicativeActTypeCode",
  "fromSendingSystem",
  "messageType",
  "replyToSystem",
  "securityClassificationCode",
  "systemMessageType",
  "toReceivingSystem",
] as const;

export const HLA_CLAIM_KEYS = [
  "operations",
  "federateName",
  "federateType",
  "federations",
  "interest",
] as const;

export type C2simClaimKey = (typeof C2SIM_CLAIM_KEYS)[number];

export type ClientConfig = {
  name: string;
  expireTimeInSeconds: number;
  useHlaClaims: boolean;
  useC2simClaims: boolean;

  hlaClaims?: Record<string, string>;
  c2simClaims?: Partial<Record<C2simClaimKey, string>>;
};
