## 1. Identity

| Field | Value |
|---|---|
| Name | adempiere-payroll-multi-engine |
| Repository Type | Library |
| Classification basis | `.ai/repository.yml` declares `type: Library`. |
| Standards | knowledge-contract-v1, repository-classification-v1 |
| Component type | Java library for ADempiere payroll engine extension |
| Language and target | Java, `sourceCompatibility = 1.17` |
| Build / runtime | Gradle wrapper 7.3.3; CI builds with Temurin JDK 17 |
| Published artifact | `io.github.adempiere:adempiere-payroll-multi-engine` |
| Version | Git tags `ERP-1.1.9`, `ERP-1.2.0`, `ERP-1.2.1`; build version from `ADEMPIERE_LIBRARY_VERSION`, default `local-1.0.0` |
| License | GNU General Public License v2 |
| Root package or module | `org.spin.eca59` |
| Declared entity type | `D` |
| Owner | ERP Consultores y Asociados |
| Upstream | https://github.com/adempiere/adempiere-payroll-multi-engine |

## 2. Responsibility

This repository provides a reusable ADempiere payroll multi-engine library. It defines the `PayrollEngine` contract, a factory that selects an engine implementation, a default parallel engine, and the wrappers/helpers used by payroll rules to read concepts, employees, processes, attributes, movements, and commissions.

What it owns:

- The public Java API in `org.spin.eca59.*`.
- The default parallel payroll engine implementation in `org.spin.eca59.engine.parallel`.
- Payroll rule execution contracts in `org.spin.eca59.rule`.
- The migration that inserts this library's `AD_EntityType` record.

What it does not own:

- ADempiere Base and its core model.
- The human resource and payroll dictionary tables (`HR_*`) themselves.
- UI or service endpoints.
- Customer-specific payroll rules or customer customizations.

## 3. Architecture

```text
src/main/java/org/spin/eca59/
  concept/
    PayrollConcept
  employee/
    PayrollEmployee
  engine/
    EngineHelper
    PayrollEngine
    PayrollEngineFactory
    parallel/
      ParallelContext
      ParallelEngine
      ParallelHelper
  payroll_process/
    PayrollProcess
  rule/
    DefaultRuleResult
    RuleContext
    RuleResult
    RuleRunner
    RuleRunnerFactory
xml/migration/
  10450_ERPYA_Add_Payroll_Multi_Engine_Entity_Type.xml
```

Patterns and extension points:

- `PayrollEngineFactory` reads the ADempiere system configuration key `ECA59_PAYROLL_ENGINE`. If no class is configured, it instantiates `org.spin.eca59.engine.parallel.ParallelEngine`.
- Engine implementations are expected to implement `PayrollEngine` and provide a constructor accepting `MHRProcess`.
- `RuleRunnerFactory` loads `RuleRunner` implementations for generated rule classes.
- `ParallelEngine` runs payroll employees through a fixed thread pool, executes payroll concepts per employee, and creates `HR_Movement` records.
- `ParallelHelper` implements `EngineHelper` and provides the helper methods available to payroll rules.
- `RuleContext` supplies the current engine, process, employee, concept, helper, and break methods to rules.

### Pre-existing records this repository modifies

| Record | Table | Columns changed | Effect | Migration |
|---|---|---|---|---|
| None | — | — | — | — |

The only migration action in evidence is an insert into `AD_EntityType` with `Record_ID` 50164, which did not pre-exist this repository. No pre-existing dictionary record is updated.

## 4. Dependencies

| Dependency | Scope | Purpose |
|---|---|---|
| `io.github.adempiere:base:3.9.4` | build/runtime | ADempiere core model and utilities used by the engine |
| `io.github.adempiere:human-resource-and-payroll:3.9.4` | build/runtime | HR and payroll models such as `MHRProcess`, `MHRMovement`, `MHRConcept`, and payroll services |
| Gradle wrapper 7.3.3 | build | Build and publication environment |
| Maven Central / GitHub Packages repository | publish | Artifact distribution |
| ADempiere `MSysConfig` key `ECA59_PAYROLL_ENGINE` | runtime integration | Selects the payroll engine class used by `PayrollEngineFactory` |
| `lib/*.jar` fileTree | build/runtime | Optional local jars; no `lib/` files are tracked in evidence |

## 5. Consumers

- ADempiere payroll components that call `PayrollEngineFactory.getInstance(MHRProcess)` against the published artifact.
- Payroll rule scripts (`Scriptlet` and JSR 223 rules) that execute inside `ParallelEngine` and use `RuleContext`/`EngineHelper`.
- Implementations outside this repository that implement `PayrollEngine` or `RuleRunner`.
- The ADempiere dictionary installer, through `xml/migration/10450_ERPYA_Add_Payroll_Multi_Engine_Entity_Type.xml`.

Changing public API signatures, artifact coordinates, or the installed migration record can break these consumers at compile time, runtime, or dictionary install time.

## 6. Allowed changes

- Bug fixes and internal improvements to `ParallelEngine` and `ParallelHelper` that preserve existing public behavior.
- Adding new `PayrollEngine` implementations with a constructor accepting `MHRProcess`.
- Adding new `RuleRunner` implementations that implement the existing `RuleRunner` contract.
- Updating build and publication configuration while preserving the published coordinates.
- Adding additive migration XML for records owned by this library.
- Updating documentation to match verified behavior.

## 7. Prohibited changes

- Do not change the published Maven coordinates `io.github.adempiere:adempiere-payroll-multi-engine`.
- Do not remove or rename public types in `org.spin.eca59.*`, especially `PayrollEngine`, `EngineHelper`, `RuleContext`, `RuleRunner`, and their methods, without a consumer-compatible path.
- Do not modify or delete the existing migration `10450_ERPYA_Add_Payroll_Multi_Engine_Entity_Type.xml`; corrections require a new additive migration.
- Do not add customer-specific behavior, tenant-specific rules, or dependencies on `PatchCustomer`.
- Do not commit credential values, local build output, or new IDE metadata.
- Do not turn this library into a standalone service or UI component.

## 8. Architectural rules

1. Every payroll engine implementation must implement `PayrollEngine` and expose a constructor `(MHRProcess)`.
2. `PayrollEngineFactory` remains the single entry point for obtaining an engine, and the lookup key remains `ECA59_PAYROLL_ENGINE`.
3. Public contracts used by rules must remain stable: changes to `EngineHelper`, `RuleContext`, `PayrollEngine`, `PayrollConcept`, `PayrollEmployee`, or `PayrollProcess` are breaking unless compatibility is explicit.
4. Dictionary changes remain additive migration XML only.
5. Publication keeps `groupId` `io.github.adempiere` and `artifactId` `adempiere-payroll-multi-engine`.
6. The package keeps a library layout: Java API and dictionary metadata only, with no service or UI layer.

## 9. Risks

### Mandatory checks

| Check | Finding | Impact | Precaution |
|---|---|---|---|
| Identifiers outside the allowed allocation range | No allocation range is declared anywhere in this repository's evidence; the migration creates `AD_EntityType` Record_ID 50164, so the claim cannot be evaluated. | Allocation validity remains unverified for release evaluation. | Declare the allowed identifier range for entity type `D`, or confirm that 50164 is allotted. |
| Build output or IDE metadata under version control | Tracked IDE metadata includes `.classpath`, `.project`, `.settings/`, and `.vscode/settings.json`. | Local IDE differences can make checkouts dirty and obscure real changes. | Remove them from tracking or deliberately accept them as shared configuration. |
| Secrets in the tree or recoverable from history | None found. Masked entries under `.github/workflows/` and `build.gradle` are environment/key references; no actual secret value is present in the tracked files shown. | No exposure established. | Keep credentials in CI secrets/environment variables; do not write real values into tracked files. |
| Absent verification mechanism | No test sources or test dependencies appear in evidence. CI runs `./gradlew build`, but only compilation is evidenced. | Payroll calculation defects can reach consumers unnoticed. | Use the `erp-ai:candidate` / `erp-ai:verified` workflow to obtain human verification before treating a change as safe. |
| Pre-existing records modified (cross-reference section 3) | None found. | No direct dictionary compatibility risk. | N/A |

### Other known risks

| Risk | Impact | Precaution |
|---|---|---|
| The migration inserts `AD_EntityType` Record_ID 50164 with `EntityType=ECA59`, while `.ai/repository.yml` and `build.gradle` declare entity type `D`. | The installed dictionary record may be attributed to the wrong or a legacy entity type, affecting model behavior or release evaluation. | The owner should confirm whether `ECA59` is deliberate or correct it with a new additive migration. |
| `README.md` says JDK 11 or later, but `build.gradle` sets `sourceCompatibility = 1.17` and CI uses JDK 17. | Consumers running Java 11 may be unable to load the jar. | Update the README to Java 17, or restore Java 11 compatibility if that is the intended contract. |
| `ParallelEngine` runs payroll calculations concurrently and shares synchronized caches, but no automated tests are present. | Race conditions or incorrect payroll movements are possible. | Validate changes with realistic multi-employee payroll runs; prefer small, reviewable changes. |
| `README.md` points to `central.sonatype.com` for the artifact, while `build.gradle` defaults publication to `https://maven.pkg.github.com/erpcya/adempiere-payroll-multi-engine`. | Consumers may look for releases in the wrong repository. | Verify the actual publish destination per release and align the README. |
| `README.md` says Gradle 8.0.1 or later, but the tracked wrapper is 7.3.3. | Developers may use inconsistent Gradle versions. | Align the README with the wrapper actually used. |

## 10. Current state

The repository is a forked Java library with default branch `erpya`. It declares itself as `Library` and uses entity type `D`. The build is Gradle-based, and the artifact is published under `io.github.adempiere:adempiere-payroll-multi-engine`, by default to GitHub Packages for the `erpcya` fork.

The source provides a working payroll multi-engine API and a default `ParallelEngine`, including rule execution through `RuleContext`, `EngineHelper`, and `RuleRunner`. The migration inserts one `AD_EntityType` record with `Record_ID` 50164. Evidence shows no updates to pre-existing dictionary records.

Known gaps include the absence of tracked tests, tracked Eclipse metadata, the entity-type discrepancy between the migration record and the declared entity type, and documentation mismatches for JDK and Gradle versions.

## 11. UNKNOWN

- Whether the `AD_EntityType` record with `EntityType=ECA59` is intentional or an error; verify with the repository owner against the migration source.
- Whether releases are actually published to Maven Central as the README states, or only to the GitHub Packages destination configured in `build.gradle`; verify in the package registry.
- Whether Java 11 compatibility is still required, given the README and the Java 17 build configuration conflict.
- Whether any automated tests exist outside the tracked tree.
- The allowed dictionary identifier allocation range for entity type `D`; not declared in the repository evidence.
- Whether `.vscode/settings.json` is intended shared team configuration or accidental local IDE state.