# C2SIM XML Message examples

## Schema version

The examples are based on schema version '1.0.2' (used in CWIX 2025). Due to an XSD schema publishing error the '1.0.2' schema version still uses namespace `http://www.sisostds.org/schemas/C2SIM/1.1` namespace.

## Submit Initialization

```
<Message xmlns="http://www.sisostds.org/schemas/C2SIM/1.1">
    <C2SIMHeader>
        <CommunicativeActTypeCode>Accept</CommunicativeActTypeCode>
        <ConversationID>ab8ba502-b14b-4385-9741-c117c2ef4e55</ConversationID>
        <FromSendingSystem>LOX</FromSendingSystem>
        <MessageID>df0c8d2a-a098-4fcb-8e44-42ff4f9fb9df</MessageID>
        <Protocol>SISO-STD-C2SIM</Protocol>
        <ProtocolVersion>1.0.2</ProtocolVersion>
        <SecurityClassificationCode>Unclassified</SecurityClassificationCode>
        <SendingTime>
            <IsoDateTime>2025-10-26T08:53:40Z</IsoDateTime>
        </SendingTime>
        <ToReceivingSystem>LOX</ToReceivingSystem>
    </C2SIMHeader>
    <MessageBody>
        <SystemMessageBody>
            <SubmitInitialization/>
        </SystemMessageBody>
    </MessageBody>
</Message>
```

## Share Scenario

```
<Message xmlns="http://www.sisostds.org/schemas/C2SIM/1.1">
    <C2SIMHeader>
        <CommunicativeActTypeCode>Accept</CommunicativeActTypeCode>
        <ConversationID>b0c5ecc1-c589-4e53-8ae0-6e46eadf338e</ConversationID>
        <FromSendingSystem>LOX</FromSendingSystem>
        <MessageID>14ab2767-df03-4de0-994c-85593c9a4813</MessageID>
        <Protocol>SISO-STD-C2SIM</Protocol>
        <ProtocolVersion>1.0.2</ProtocolVersion>
        <SecurityClassificationCode>Unclassified</SecurityClassificationCode>
        <SendingTime>
            <IsoDateTime>2025-10-26T08:58:48Z</IsoDateTime>
        </SendingTime>
        <ToReceivingSystem>LOX</ToReceivingSystem>
    </C2SIMHeader>
    <MessageBody>
        <SystemMessageBody>
            <ShareScenario/>
        </SystemMessageBody>
    </MessageBody>
</Message>
```

## Start Scenario

```
<Message xmlns="http://www.sisostds.org/schemas/C2SIM/1.1">
    <C2SIMHeader>
        <CommunicativeActTypeCode>Accept</CommunicativeActTypeCode>
        <ConversationID>7083dc5c-64ea-4b91-80df-cb7308209167</ConversationID>
        <FromSendingSystem>LOX</FromSendingSystem>
        <MessageID>3d4802f3-35c0-414e-90ac-bd51aee6d3ef</MessageID>
        <Protocol>SISO-STD-C2SIM</Protocol>
        <ProtocolVersion>1.0.2</ProtocolVersion>
        <SecurityClassificationCode>Unclassified</SecurityClassificationCode>
        <SendingTime>
            <IsoDateTime>2025-10-26T09:00:55Z</IsoDateTime>
        </SendingTime>
        <ToReceivingSystem>LOX</ToReceivingSystem>
    </C2SIMHeader>
    <MessageBody>
        <SystemMessageBody>
            <StartScenario/>
        </SystemMessageBody>
    </MessageBody>
</Message>
```

## Task status

| ENUM      | Description |
| --------- | ----------- |
| TASKABRT  | ABORT       |
| TASKCMPLT | COMPLETE    |
| TASKINPRG | IN PROGRESS |
| TASKPEND  | PENDING     |
| TASKSTRT  | START       |

```
<Message xmlns=http://www.sisostds.org/schemas/C2SIM/1.1>
    <C2SIMHeader>
        <CommunicativeActTypeCode>Accept</CommunicativeActTypeCode>
        <ConversationID>6fcf6992-863f-4c29-8082-c17b3cc7c07b</ConversationID>
        <FromSendingSystem>SYSTEM_A</FromSendingSystem>
        <MessageID>b662ebd4-773e-4212-adc3-6301f2e5a9b9</MessageID>
        <Protocol>SISO-STD-C2SIM</Protocol>
        <ProtocolVersion>1.0.2</ProtocolVersion>
        <SecurityClassificationCode>Unclassified</SecurityClassificationCode>
        <SendingTime>
            <IsoDateTime>2025-10-31T15:12:58Z</IsoDateTime>
        </SendingTime>
        <ToReceivingSystem>SYSTEM_A</ToReceivingSystem>
    </C2SIMHeader>
    <MessageBody>
        <DomainMessageBody>
            <ReportBody>
                <FromSender>71c32ed3-810b-490a-b6a7-bbe231534de6</FromSender>
                <ToReceiver>c4ed6e33-3749-4fa0-9db8-5b52bce57d89</ToReceiver>
                <ReportContent>
                    <TaskStatus>
                        <TimeOfObservation>
                            <DateTime>
<IsoDateTime>2025-10-31T15:12:58Z</IsoDateTime>
                            </DateTime>
                        </TimeOfObservation>
                        <CurrentTask>240d4341-b754-42a8-8a4a-cf551ed40761</CurrentTask>
                        <TaskStatusCode>TASKSTRT</TaskStatusCode>
                    </TaskStatus>
                </ReportContent>
                <ReportID>ad39030c-d3f1-4abf-8a27-88136ce85b31</ReportID>
                <ReportingEntity>683dd723-f408-406f-92c1-cdea10130966</ReportingEntity>
            </ReportBody>
        </DomainMessageBody>
    </MessageBody>
</Message>
```

## Position report

```
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<Message xmlns="http://www.sisostds.org/schemas/C2SIM/1.1">
    <C2SIMHeader>
        <CommunicativeActTypeCode>Accept</CommunicativeActTypeCode>
        <ConversationID>c2f731c3-b069-4623-98c8-9c620898b459</ConversationID>
        <FromSendingSystem>SYSTEM_A</FromSendingSystem>
        <MessageID>3f979f94-8732-4131-85fe-54de091b30d5</MessageID>
        <Protocol>SISO-STD-C2SIM</Protocol>
        <ProtocolVersion>1.0.2</ProtocolVersion>
        <SecurityClassificationCode>Unclassified</SecurityClassificationCode>
        <SendingTime>
            <IsoDateTime>2025-10-31T14:59:59Z</IsoDateTime>
        </SendingTime>
        <ToReceivingSystem>SYSTEM_A</ToReceivingSystem>
    </C2SIMHeader>
    <MessageBody>
        <DomainMessageBody>
            <ReportBody>
                <FromSender>65556e95-7c5e-4889-9e86-40cc3a04b32f</FromSender>
                <ToReceiver>00000000-0000-0000-0000-000000000000</ToReceiver>
                <ReportContent>
                    <PositionReportContent>
                        <TimeOfObservation>
                            <DateTime>
                                <IsoDateTime>2025-10-31T14:59:59Z</IsoDateTime>
                            </DateTime>
                        </TimeOfObservation>
                        <EntityHealthStatus>
                            <OperationalStatus>
                                <OperationalStatusCode>FullyOperational</OperationalStatusCode>
                            </OperationalStatus>
                        </EntityHealthStatus>
                        <EntityHealthStatus>
                            <Strength>
                                <StrengthPercentage>43</StrengthPercentage>
                            </Strength>
                        </EntityHealthStatus>
                        <HeadingAngle>23.0</HeadingAngle>
                        <Location>
                            <GeodeticCoordinate>
                                <AltitudeAGL>0.0</AltitudeAGL>
                                <Latitude>3.0</Latitude>
                                <Longitude>33.0</Longitude>
                            </GeodeticCoordinate>
                        </Location>
                        <Speed>37.0</Speed>
                        <SubjectEntity>fa655ba7-5f75-4d9d-bbee-e871b33c8ae2</SubjectEntity>
                    </PositionReportContent>
                </ReportContent>
                <ReportContent>
                    <PositionReportContent>
                        <TimeOfObservation>
                            <DateTime>
                                <IsoDateTime>2025-10-31T14:59:59Z</IsoDateTime>
                            </DateTime>
                        </TimeOfObservation>
                        <EntityHealthStatus>
                            <OperationalStatus>
                                <OperationalStatusCode>FullyOperational</OperationalStatusCode>
                            </OperationalStatus>
                        </EntityHealthStatus>
                        <EntityHealthStatus>
                            <Strength>
                                <StrengthPercentage>6</StrengthPercentage>
                            </Strength>
                        </EntityHealthStatus>
                        <HeadingAngle>226.0</HeadingAngle>
                        <Location>
                            <GeodeticCoordinate>
                                <AltitudeAGL>0.0</AltitudeAGL>
                                <Latitude>-11.0</Latitude>
                                <Longitude>83.0</Longitude>
                            </GeodeticCoordinate>
                        </Location>
                        <Speed>21.0</Speed>
                        <SubjectEntity>dd972998-b6ae-4f93-b06a-3aa1ca5073f8</SubjectEntity>
                    </PositionReportContent>
                </ReportContent>
                <ReportID>6ff98ad9-d0a4-4343-8fa9-6ed797cd0288</ReportID>
                <ReportingEntity>6daf87f1-0015-4b44-b378-e3ac140f45c8</ReportingEntity>
            </ReportBody>
        </DomainMessageBody>
    </MessageBody>
</Message>
```

## Task order

| ENUM (most used TaskActionCode) |     |
| ------------------------------- | --- |
| MoveToLocation                  |     |
| ATTACK                          |     |
| DEFEND                          |     |
| SCOUT                           |     |

```
<Message xmlns=http://www.sisostds.org/schemas/C2SIM/1.1>
    <C2SIMHeader>
        <CommunicativeActTypeCode>Accept</CommunicativeActTypeCode>
        <ConversationID>c2a90bdc-76b3-45d8-8d2a-4c97fe1dade0</ConversationID>
        <FromSendingSystem>TEST</FromSendingSystem>
        <MessageID>31cd312b-0114-4e39-8bab-a40682d97e79</MessageID>
        <Protocol>SISO-STD-C2SIM</Protocol>
        <ProtocolVersion>1.0.2</ProtocolVersion>
        <SecurityClassificationCode>Unclassified</SecurityClassificationCode>
        <SendingTime>
            <IsoDateTime>2025-10-31T15:27:51Z</IsoDateTime>
        </SendingTime>
        <ToReceivingSystem>TEST</ToReceivingSystem>
    </C2SIMHeader>
    <MessageBody>
        <DomainMessageBody>
            <OrderBody>
                <FromSender>00000000-0000-0000-0000-000000000000</FromSender>
                <ToReceiver>00000000-0000-0000-0000-000000000000</ToReceiver>
                <IssuedTime>
                    <IsoDateTime>2025-10-31T15:27:51Z</IsoDateTime>
                </IssuedTime>
                <OrderID>73f1b4f4-6225-40a0-8ae1-517f6ead90b4</OrderID>
                <Task>
                    <ManeuverWarfareTask>
                        <Location>
                            <GeodeticCoordinate>
<Latitude>34.0</Latitude>
<Longitude>56.0</Longitude>
                            </GeodeticCoordinate>
                        </Location>
                        <UUID>ee99e41c-8f31-49c3-b705-d37c4d598c40</UUID>
                        <PerformingEntity>3f92a4b1-2c5d-4a08-bf61-8a79b62c9384</PerformingEntity>
                        <TaskActionCode>MoveToLocation</TaskActionCode>
                    </ManeuverWarfareTask>
                </Task>
            </OrderBody>
        </DomainMessageBody>
    </MessageBody>
</Message>
```

## Magic move (teleport)

```
    <C2SIMHeader>
        <CommunicativeActTypeCode>Accept</CommunicativeActTypeCode>
        <ConversationID>7eda75a9-bcc2-4921-ae0f-6b3cd6b84676</ConversationID>
        <FromSendingSystem>SYSTEM_A</FromSendingSystem>
        <MessageID>0b4b29b7-1fb7-45c3-ba30-73100623a417</MessageID>
        <Protocol>SISO-STD-C2SIM</Protocol>
        <ProtocolVersion>1.0.2</ProtocolVersion>
        <SecurityClassificationCode>Unclassified</SecurityClassificationCode>
        <SendingTime>
            <IsoDateTime>2025-10-29T15:42:40Z</IsoDateTime>
        </SendingTime>
        <ToReceivingSystem>SYSTEM_A</ToReceivingSystem>
    </C2SIMHeader>
    <MessageBody>
        <SystemMessageBody>
            <MagicMove>
                <EntityReference>f8237259-9a3c-4939-ac2c-d188736386a2</EntityReference>
                <Location>
                    <GeodeticCoordinate>
                        <Latitude>10.0</Latitude>
                        <Longitude>20.0</Longitude>
                    </GeodeticCoordinate>
                </Location>
            </MagicMove>
        </SystemMessageBody>
    </MessageBody>
</Message>
```
