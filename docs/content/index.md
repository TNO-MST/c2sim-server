# C2SIM-SERVER

This project provides the **reference implementation of the C2SIM Server**, acting as a **central distribution hub** for interoperable Command and Control (C2), simulation, and robotic/autonomous systems.

The server is based on the **SISO C2SIM standards**:

- **SISO-STD-019-2020** defines standardized interoperability between **C2 systems**, **simulation systems**, and **robotic/autonomous systems (RAS)**.

- **SISO-STD-020-2020** extends the core standard with **land warfare–specific concepts and data models**.

The **C2SIM Server** enables multiple heterogeneous systems to exchange initialization data, orders, requests, and reports through a **common semantic and messaging framework**, reducing the need for point-to-point integrations.

This implementation is the successor to the [GMU C2SIM Server](https://github.com/OpenC2SIM/OpenC2SIM.github.io), continuing and modernizing the reference architecture originally developed by George Mason University.



## SISO Standards

| Standard                                                                                                                            | Name                                                                                                            |
| ----------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| [SISO-STD-019-2020](https://cdn.ymaws.com/www.sisostandards.org/resource/resmgr/standards_products/siso-std-019-2020_c2sim.pdf)     | Standard for Command and Control Systems - Simulation Systems Interoperation                                    |
| [SISO-STD-020-2020](https://cdn.ymaws.com/www.sisostandards.org/resource/resmgr/standards_products/siso-std-020-2020_lox-c2sim.pdf) | Standard for Land Operations Extension (LOX) to Command and Control Systems - Simulation Systems Interoperation |

### SISO-STD-019-2020: C2 Systems - Simulation Systems Interoperation

Defines a standardized model and data exchange format for information between Command and Control (C2) systems, simulation systems, and robotic/autonomous systems (RAS), especially in coalition military contexts.

**Key Definitions**

- **Interoperability Focus:** Enables structured initialization, tasking (orders/requests), and reporting data exchange among heterogeneous systems.

- **Logical Data Model (LDM):**
  
  - Expressed as an **ontology** using the **Web Ontology Language (OWL)**.
  
  - Includes a **core ontology** and optional extensions for domain-specific content.

**Core Capabilities Defined**

- **Common Data Model:** Shared classes and properties that represent operational and force information, orders, reports, and situational context.

- **Message Structure and Exchange:** Uses FIPA ACL-style messaging semantics over XML schemas derived from the ontologies.

- **Extensibility:** Through higher-level ontologies layered on the core LDM.

### SISO-STD-020-2020: Land Operations Extension (LOX)

**Purpose & Scope**

- Extends the **C2SIM Core** for **land warfare operations**, adding the specific data constructs required to represent **plans, orders, and domain-specific entities** used in land operations.

- Requires the **C2SIM core and the Standard Military Extension (SMX)** as prerequisites — LOX builds on them.

**Key Features**

- **Land Domain Ontology:** Defines the additional classes and relationships relevant to surface forces and their operations (terrain, units, movements, etc.).

- **Integration with Core:** It is not standalone; the extension imports and references the core ontology and SMX.

- **XML Schema Generation:** Extensions can produce XML schemas from the ontologies for actual message/data exchange.