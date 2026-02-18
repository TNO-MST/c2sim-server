# C2SIM Specification — in a nutshell

## Purpose

The [C2SIM standard](https://www.sto.nato.int/document/overview-of-c2-systems-simulation-systems-interoperation-c2sim-standard/) standardizes interoperability between `Command and Control (C2) systems` and `simulation systems`.

A key focus of the standard is the `exchange of C2SIM messages` between systems. The `C2SIM server` acts as a mediator, enabling systems to exchange information in a consistent and interoperable manner.

## Vision

The [MSG-211 Report](https://publications.sto.nato.int/publications/STO%20Educational%20Notes/STO-EN-MSG-211/EN-MSG-211-1.3.pdf) describes the vision as follows:

> *We are working toward a day when the members of a coalition interconnect their networks, command and control (C2) systems, and simulations simply by turning them on and authenticating, in a standards-based environment.*

The vision behind C2SIM is seamless interoperability within a coalition environment, where:

- Nations use their own C2 systems
- Systems interoperate using shared C2 standards
- National simulations interoperate using Modeling & Simulation (M&S) standards, such as:
  - HLA (High Level Architecture)
  - DIS (Distributed Interactive Simulation)

The term **“Coalition”** reflects a higher level of integration than traditional *federated* simulation environments such as HLA. In C2SIM, interoperability occurs at the operational C2 level across a *system of systems*.

## SISO Standards

The SISO standards define the information must be exchanged at the system level between C2 and simulation systems.

The C2SIM standard specifies the “what”, not the “how” (i.e., it does not prescribe specific transport mechanisms or system implementations).

| Standard                                                                                                                            | Name                                                                           |
| ----------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------ |
| [SISO-STD-019-2020](https://cdn.ymaws.com/www.sisostandards.org/resource/resmgr/standards_products/siso-std-019-2020_c2sim.pdf)     | *Standard for Command and Control Systems – Simulation Systems Interoperation* |
| [SISO-STD-020-2020](https://cdn.ymaws.com/www.sisostandards.org/resource/resmgr/standards_products/siso-std-020-2020_lox-c2sim.pdf) | *Standard for Land Operations Extension (LOX) to C2SIM*                        |

## C2SIM Ontology

The C2SIM standard defines a data model that specifies the information to be shared across C2SIM systems to ensure interoperability.

This data model is formally described in the `C2SIM Ontology`.

The ontology is based on:

- Core Ontology with a Standard Military Extension (SMX)
- Extended by the Land Operations Extension (LOX)

### Technical Representation

The ontology is defined in `RDF/XML` format. An `XSLT transformation` converts the RDF/XML representation into an `XML Schema Definition (XSD)`. 

Between the C2SIM systems `XML` messages are exchanged.

In this project the `XSD` is used for code generation and validation of XML messages.
