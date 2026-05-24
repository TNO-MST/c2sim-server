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
      fromSendingSystem: "NLD-TAC",
      messageType: "C2SIMInitialization;DomainMessage;ObjectInitialization;SystemAcknowledgement;SystemMessage",
      replyToSystem: "ANY",
      systemMessageType: "ANY",
	  toReceivingSystem: "ANY"
    },
  }
];






