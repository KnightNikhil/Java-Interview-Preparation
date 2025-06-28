✅ 1. Build Tool Fundamentals
* What is a build tool?
* Role of Maven and Gradle in:
* Compiling code
* Resolving dependencies
* Running tests
* Packaging (JAR/WAR)
* Deployment

⸻

✅ 2. Maven

🔹 Basics
* pom.xml structure
* Dependencies, plugins, repositories
* Lifecycle phases: validate, compile, test, package, verify, install, deploy
* Maven goals and phases

🔹 Dependency Management
* Transitive dependencies
* Scope: compile, provided, runtime, test, system, import
* BOM (Bill of Materials) and version alignment
* Dependency exclusion and conflict resolution

🔹 Plugins & Profiles
* Common plugins: maven-compiler-plugin, maven-surefire-plugin, maven-jar-plugin, maven-deploy-plugin
* Build profiles for different environments (e.g., dev, prod)
* Maven Wrapper (mvnw)

🔹 Customization & Advanced
* Parent POMs and inheritance
* Multi-module project structure
* Property placeholders and filtering

⸻

✅ 3. Gradle

🔹 Basics
* build.gradle (Groovy) / build.gradle.kts (Kotlin DSL)
* Tasks and plugins
* Lifecycle phases: clean, build, test, assemble, etc.
* Gradle Wrapper (gradlew)

🔹 Dependency Management
* Repositories: Maven Central, JCenter, custom
* Dependency scopes/configurations: implementation, api, compileOnly, runtimeOnly, testImplementation
* Version alignment, resolution strategy
* Custom dependency exclusions

🔹 Build Scripts & Tasks
* Creating custom tasks
* Task dependencies and ordering
* Writing reusable build logic

🔹 Plugins & Profiles
* Popular plugins: java, application, jacoco, spring-boot
* Profile-like configuration using project.hasProperty() or gradle.properties

🔹 Multi-Module Builds
* settings.gradle file
* Shared dependencies and task configuration
* Dependency substitution and project isolation

⸻

✅ 4. Maven vs Gradle

-

✅ 5. Best Practices
* Use Maven for stability and standard structure
* Use Gradle for flexibility and faster builds
* Lock dependency versions (dependency locking or BOM)
* Avoid unnecessary transitive dependencies
* Use the wrapper for consistent builds across teams