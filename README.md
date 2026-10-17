# Enterprise Mainframe CI/CD Pipeline with IBM DBB and Ansible

## Architectural Overview
This repository implements a production-grade, end-to-end **Mainframe DevOps CI/CD pipeline** for compiling, packaging, publishing, and deploying COBOL workloads on **IBM z/OS USS (UNIX System Services)**. Leveraging **IBM Dependency Based Build (DBB) v2.0**, **JFrog Artifactory**, and **Red Hat Ansible Certified Content for IBM z/OS**, this project automates legacy modernization while enforcing strict MVS data set allocation policies, atomic builds, and automated CICS region refreshing.


```

[Git Repository]
│
▼
[DBB v2.0 Compile (IGYCRCTL) & Link (IEWBLINK)]
│
▼
[Binary Extraction to USS & Manifest Generation]
│
▼
[JFrog Artifactory Publication via cURL]
│
▼
[Ansible Orchestration: zos_copy & CICS NEWCOPY]

```

---

## 1. THE COMPLETE PROJECT REPOSITORY ARCHITECTURE

### Directory Structure

```text
mainframe-cicd-pipeline/
├── cobol/
│   └── SAM1.cbl
├── copybook/
│   └── SAMCOPY.cpy
├── build.properties
├── build.groovy
├── package.groovy
├── cleanup.groovy
├── ansible/
│   └── deploy.yml
├── .github/
│   └── workflows/
│       └── pipeline.yml
├── .gitlab-ci.yml
├── sonar-project.properties
└── .gitignore

```

---

## Core Technical Prerequisites
* **Operating System**: z/OS 2.5 or higher with USS enabled.
* **Java Runtime**: IBM Semeru Runtime Certified Edition for z/OS (Java 11).
* **Build Engine**: IBM Dependency Based Build (DBB) v2.0.2.7.
* **Compilers & Linkers**: IBM Enterprise COBOL (`IGYCRCTL`) & Binder (`IEWBLINK`).
* **Automation**: Red Hat Ansible Core with `ibm.ibm_zos_core` collection installed.

## Configuration Instructions
1. Clone the repository into your z/OS USS working path:
```bash
   git clone [https://github.com/your-org/mainframe-cicd-pipeline.git](https://github.com/your-org/mainframe-cicd-pipeline.git)
   cd mainframe-cicd-pipeline

```

---

2. Modify `build.properties` to set your target uppercase High-Level Qualifier (`hlq`) and local storage parameters.
3. Export your Java environment paths and execute a test build:
```bash
export JAVA_HOME=/usr/lpp/java/J11.0_64
groovy build.groovy .

```

---

## Production Troubleshooting Guide

### 1. Return Code 16 Formatting Fixes in IGYCRCTL

* **Symptom**: Compilation aborts with RC 16 due to card format truncation.
* **Resolution**: Ensure all source files and copybooks (`SAM1.cbl`, `SAMCOPY.cpy`) include the correct 7-character padding layout ensuring code execution aligns strictly to Area A/B beginning at column 8.

### 2. Missing SYSUT Allocations in DBB MVSExec

* **Symptom**: Binder or Compiler fails with `IEW0000` or `IGYDS0000` missing scratch pad error.
* **Resolution**: Ensure temporary work datasets (`SYSUT1` through `SYSUT7`) are explicitly declared using track space sizing (`TRK, (15,15)`) inside `build.groovy`.

### 3. BPXWDYN Track Allocation Sequence Syntax Errors

* **Symptom**: `BPXWDYN` returns error code `IKJ56703I` invalid parameter sequence.
* **Resolution**: Maintain strictly formatted uppercase qualifiers and explicit track allocation variables in `build.properties` formatted as `space(2,1) trk`.

---
