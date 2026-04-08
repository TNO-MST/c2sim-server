package org.c2sim.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.OffsetDateTime;
import java.util.List;
import org.c2sim.client.model.DynamicSessionInfo;
import org.c2sim.client.model.SessionInfo;
import org.c2sim.client.model.StateType;

class CreateJson {
  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .findAndRegisterModules()
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  static String toJson(Object obj) {
    try {
      return MAPPER.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static DynamicSessionInfo createDynamicSessionInfo(StateType state, String sharedSessionName) {

    DynamicSessionInfo session = new DynamicSessionInfo();
    session.setSessionName(sharedSessionName);
    session.setCreatedAt(OffsetDateTime.now());
    session.setState(state);

    var sessionInfo = new SessionInfo();
    sessionInfo.setDisplayName(sharedSessionName);
    sessionInfo.setDescription(sharedSessionName);
    sessionInfo.setC2simSchemaVersion("1.0.2");
    session.setInfo(sessionInfo);
    return session;
  }

  static List<DynamicSessionInfo> createSessionsDummyData(
      StateType state, String sharedSessionName) {
    return List.of(createDynamicSessionInfo(state, sharedSessionName));
  }
}
