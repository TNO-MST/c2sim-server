import type { ClientConfig } from "./types.js";

export const CLIENTS: ClientConfig[] = [
  {
    name: "NLD-LOX-1",
    expireTimeInSeconds: 3600,
    useHlaClaims: false,
    useC2simClaims: true,
    c2simClaims: {
      fromSendingSystem: "NLD-LOX",
      messageType: "C2SIMInitialization;DomainMessage;ObjectInitialization;SystemAcknowledgement;SystemMessage",
      securityClassificationCode: "UNCLASSIFIED",
      communicativeActTypeCode: "Accept;Agree;Confirm;Inform;Propose;Refuse;Request",
      replyToSystem: "ANY",
      systemMessageType: "ANY",
	  toReceivingSystem: "ANY"
    },
  },
  {
    name: "NLD-TAC2-1",
    expireTimeInSeconds: 3600,
    useHlaClaims: false,
    useC2simClaims: true,
    c2simClaims: {
      fromSendingSystem: "FRA-SWORD",
      messageType: "NLD-TAC2",
      securityClassificationCode: "Unclassified;Confidential;Secret;TopSecret",
      communicativeActTypeCode: "Accept;Agree;Confirm;Inform;Propose;Refuse;Request",
      replyToSystem: "ANY",
      systemMessageType: "ANY",
	  toReceivingSystem: "ANY"
    },
  }
];






