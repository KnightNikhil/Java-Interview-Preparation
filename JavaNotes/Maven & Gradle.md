✅ 1. Build Tool Fundamentals
* What is a build tool?
* Role of Maven and Gradle in:
* Compiling code
* Resolving dependencies
* Running tests
* Packaging (JAR/WAR)
* Deployment

---
# Build Tools Interview Guide

This guide explains the fundamentals of build tools in Java projects, with a focus on Maven and Gradle, including detailed interview questions and answers.

---

## ✅ 1. Build Tool Fundamentals

### 🔹 What is a build tool?
A build tool automates the process of converting source code into a deployable artifact (like JAR or WAR), including compiling code, resolving dependencies, running tests, and packaging.

Popular build tools in Java:
- **Maven**: XML-based configuration (`pom.xml`), convention over configuration.
- **Gradle**: Groovy/Kotlin DSL-based configuration, highly customizable, faster with incremental builds.

---

### 🔹 Role of Maven and Gradle

#### 📌 Compiling Code
- **Maven**: Uses the `maven-compiler-plugin`.
  ```xml
  <plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
      <source>1.8</source>
      <target>1.8</target>
    </configuration>
  </plugin>
  ```
- **Gradle**: Default Java plugin compiles `src/main/java`.
  ```groovy
  plugins {
      id 'java'
  }
  ```

#### 📌 Resolving Dependencies
- **Maven**: Declares dependencies in `<dependencies>` block in `pom.xml`.
- **Gradle**: Uses `dependencies` block in `build.gradle`.

#### 📌 Running Tests
- **Maven**: Uses `maven-surefire-plugin` for unit tests.
- **Gradle**: Runs tests automatically via the `test` task.

#### 📌 Packaging (JAR/WAR)
- **Maven**: Uses lifecycle phases like `package` to generate JAR/WAR.
- **Gradle**: Uses the `jar` or `war` task depending on plugins applied.

#### 📌 Deployment
- **Maven**: `mvn deploy` phase to upload to remote repository.
- **Gradle**: Can deploy using custom scripts or plugins like `maven-publish`.

---

## 💡 Common Interview Follow-up Questions

### ❓ What are the advantages of Gradle over Maven?
- Faster builds due to incremental compilation and caching.
- More flexible with scripting.
- Better IDE integration and DSL options.

### ❓ When would you choose Maven over Gradle?
- When working in a team already using Maven.
- When you want convention-based and simpler builds.

### ❓ How do you create multi-module projects in Maven and Gradle?
- **Maven**: Define `<modules>` in parent `pom.xml`.
- **Gradle**: Use `settings.gradle` to include subprojects.

### ❓ How can you profile build time in Gradle?
Use:
```bash
./gradlew build --profile
```

### ❓ What is the difference between `compile`, `provided`, `runtime`, and `test` scopes in Maven?
- `compile`: Available at compile and runtime.
- `provided`: Available at compile but not bundled (e.g., servlet API).
- `runtime`: Needed only at runtime.
- `test`: Available only for tests.

---


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

---
# Maven Interview Guide

## ✅ 2. Maven

---

### 🔹 Basics

#### ❓ What is `pom.xml` structure?
The `pom.xml` (Project Object Model) is the heart of a Maven project. It defines:
- Project coordinates (groupId, artifactId, version)
- Dependencies
- Plugins
- Build configuration
- Properties
- Modules (for multi-module projects)

```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>my-app</artifactId>
  <version>1.0.0</version>
</project>
```

#### ❓ What are dependencies, plugins, and repositories?
- **Dependencies**: External libraries your project needs.
- **Plugins**: Tools that add functionality to Maven, like compiling or packaging.
- **Repositories**: Central or private stores from where Maven fetches dependencies.

#### ❓ What are Maven Lifecycle Phases?
Maven defines three lifecycles (default, clean, site). The **default** lifecycle has the following phases:
1. `validate`: validate the project is correct.
2. `compile`: compile source code.
3. `test`: run unit tests.
4. `package`: package the code into JAR/WAR.
5. `verify`: run integration checks.
6. `install`: install package into local repo.
7. `deploy`: copy to remote repo for sharing.

#### ❓ Maven Goals vs Phases
- **Goal**: a specific task like `compile`, `install`, etc.
- **Phase**: a step in the lifecycle which may invoke multiple goals.

---

### 🔹 Dependency Management

#### ❓ What are Transitive Dependencies?
When you include dependency A, which depends on B and C, those are **transitive dependencies**.

#### ❓ Maven Dependency Scopes
- `compile`: default, required for all builds.
- `provided`: provided at runtime by container (e.g., Servlet API).
- `runtime`: needed only at runtime.
- `test`: needed for testing only.
- `system`: similar to provided, but requires manual path.
- `import`: used with BOMs.

#### ❓ What is BOM (Bill of Materials)?
- A way to align versions of dependencies across modules/projects.
- Typically used with Spring Boot starter parents.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>2.7.5</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

#### ❓ Dependency Exclusion
Used to avoid pulling unnecessary transitive dependencies.

```xml
<dependency>
  <groupId>org.example</groupId>
  <artifactId>foo</artifactId>
  <exclusions>
    <exclusion>
      <groupId>commons-logging</groupId>
      <artifactId>commons-logging</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

---

### 🔹 Plugins & Profiles

#### ❓ Common Plugins
- `maven-compiler-plugin`: Compiles Java code.
- `maven-surefire-plugin`: Runs unit tests.
- `maven-jar-plugin`: Creates JAR files.
- `maven-deploy-plugin`: Deploys to remote repositories.

#### ❓ Build Profiles
Used for environment-specific builds (dev, test, prod).

```xml
<profiles>
  <profile>
    <id>prod</id>
    <properties>
      <env>production</env>
    </properties>
  </profile>
</profiles>
```

#### ❓ Maven Wrapper
The `mvnw` script ensures consistent Maven version across machines.

---

### 🔹 Customization & Advanced Topics

#### ❓ Parent POMs and Inheritance
- Used for version management and central configuration.
- Helps maintain uniformity across multi-module projects.

#### ❓ Multi-Module Projects
- Allows building several modules in a single build.
```xml
<modules>
  <module>core</module>
  <module>web</module>
</modules>
```

#### ❓ Property Placeholders
- Inject dynamic or reusable values.
```xml
<properties>
  <java.version>17</java.version>
</properties>
```

---

## 🔁 Maven Interview Follow-Up Questions

1. How do you override a dependency version coming from a parent POM?
    - Use `<dependencyManagement>` in your `pom.xml`.

2. What is the difference between dependencyManagement and dependencies section?
    - `dependencyManagement` defines versions to be used; actual inclusion must be done in `dependencies`.

3. How can you skip tests while building?
    - Use: `mvn install -DskipTests`

4. How to create an executable JAR?
    - Use the `maven-jar-plugin` and set the `mainClass`.

5. How to publish to an internal Nexus/Artifactory repository?
    - Configure `distributionManagement` in the `pom.xml`.

---

### ✅ Summary
Understanding Maven’s lifecycle, dependency management, plugin usage, and advanced customization is critical for large-scale Java project builds and automation.

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
---

# Gradle Interview Preparation Guide

---

## ✅ 3. Gradle

### 🔹 Basics

**1. What is `build.gradle` or `build.gradle.kts`?**
- `build.gradle`: Gradle build file written using Groovy DSL.
- `build.gradle.kts`: Gradle build file written using Kotlin DSL (type-safe).

**2. Tasks and Plugins**
- Gradle builds are composed of _tasks_, which are atomic units of work (e.g., compile, test).
- Plugins extend Gradle’s capabilities: e.g., `java`, `application`, `spring-boot`.

**3. Lifecycle Phases**
- **clean**: Deletes previous build outputs.
- **build**: Runs all build tasks.
- **test**: Executes unit and integration tests.
- **assemble**: Compiles and packages the project.

**4. Gradle Wrapper**
- `gradlew` and `gradlew.bat` are scripts that allow the project to be built with a specific Gradle version.
- Ensures consistent builds across different environments.

---

### 🔹 Dependency Management

**1. Repositories**
- Common: `mavenCentral()`, `jcenter()`, `google()`, and custom URLs.

**2. Configurations/Scopes**
- `implementation`: Used internally, not exposed to consumers.
- `api`: Exposed to consumers of the library.
- `compileOnly`: Only for compile-time, not available at runtime.
- `runtimeOnly`: Used only at runtime.
- `testImplementation`: Used for test dependencies.

**3. Version Alignment & Resolution Strategy**
- Use _platforms_ or _enforcedPlatform_ for version alignment.
- Define custom resolution strategies to handle conflicts.

**4. Dependency Exclusions**
```groovy
implementation('group:artifact:version') {
    exclude group: 'unwanted.group', module: 'unwanted-module'
}
```

---

### 🔹 Build Scripts & Tasks

**1. Creating Custom Tasks**
```groovy
task myTask {
    doLast {
        println 'Hello, Gradle!'
    }
}
```

**2. Task Dependencies**
```groovy
task B {
    dependsOn A
}
```

**3. Reusable Build Logic**
- Create reusable logic using custom Gradle scripts and shared configurations.
- Use `buildSrc` for creating reusable task logic.

---

### 🔹 Plugins & Profiles

**1. Popular Plugins**
- `java`, `application`, `jacoco`, `spring-boot`, etc.

**2. Profiles via Properties**
- Use `project.hasProperty('env')` to load custom behavior.
- Define properties in `gradle.properties`.

```groovy
if (project.hasProperty('env') && project.env == 'prod') {
    // production-specific logic
}
```

---

### 🔹 Multi-Module Builds

**1. `settings.gradle`**
- Lists all submodules using `include 'module-name'`.

**2. Shared Dependencies**
- Use a parent project or build script to define shared versions or configurations.

**3. Dependency Substitution**
```groovy
dependencySubstitution {
    substitute module('com.example:lib') with project(':lib')
}
```

---

## 🔁 Interview Follow-Up Questions

### Q1. What is the difference between `api` and `implementation` configurations?
- `api` exposes the dependency to downstream consumers; `implementation` keeps it internal.

### Q2. How does Gradle differ from Maven?
- Gradle uses a procedural DSL (Groovy/Kotlin) vs Maven's declarative XML.
- Faster due to incremental builds and build cache.

### Q3. What is the Gradle daemon?
- A background process that keeps Gradle "warm" to reduce startup time.

### Q4. Can we run specific tasks in Gradle?
```bash
./gradlew clean test
```

### Q5. How do you manage versions of dependencies across modules?
- Use version catalogs (`libs.versions.toml`) or a shared parent build script.

---

## ✅ Summary
Gradle offers a modern, flexible, and efficient way to build and manage dependencies in Java projects. Mastering Gradle involves understanding tasks, plugins, dependency resolution, and multi-module projects.


⸻

# ✅ 4. Maven vs Gradle

## 🔹 Core Comparison

| Feature                     | Maven                                             | Gradle                                              |
|----------------------------|---------------------------------------------------|-----------------------------------------------------|
| **Language**               | XML (pom.xml)                                     | Groovy or Kotlin DSL (build.gradle or build.gradle.kts) |
| **Build Lifecycle**        | Rigid and predefined                              | Flexible and customizable                          |
| **Performance**            | Slower (due to XML parsing, lack of build caching) | Faster (incremental build and build caching)        |
| **Dependency Management**  | Uses Maven Central, transitive dependency support | Same support, but more flexible                     |
| **Plugin Ecosystem**       | Mature, rich set of plugins                       | Growing fast, can use both Gradle and Maven plugins |
| **Multi-module support**   | Strong but verbose                                | More flexible, better support with Kotlin DSL       |
| **Custom Logic**           | Difficult to implement                            | Easily written in Groovy/Kotlin                    |
| **Wrapper Support**        | Maven Wrapper (mvnw)                              | Gradle Wrapper (gradlew)                           |
| **IDE Integration**        | Excellent                                         | Excellent                                           |
| **Documentation**          | Extensive                                         | Improving, community-supported                      |

## 🔹 When to Use Which?

### Choose Maven when:
- You prefer convention over configuration.
- XML readability and structure is important for your team.
- Your team has existing expertise with Maven.
- You rely on strict lifecycle phases.

### Choose Gradle when:
- You want faster builds with incremental and cached results.
- Your build logic is complex and needs scripting.
- You prefer concise DSL over verbose XML.
- You work with Android (default build system is Gradle).

## 🔹 Common Commands Comparison

| Operation        | Maven Command                     | Gradle Command                |
|------------------|-----------------------------------|-------------------------------|
| Clean Build      | `mvn clean install`               | `./gradlew clean build`       |
| Run Tests        | `mvn test`                        | `./gradlew test`              |
| Package          | `mvn package`                     | `./gradlew assemble`          |
| Run Application  | Use plugin (e.g., spring-boot)    | `./gradlew bootRun` (Spring)  |
| Skip Tests       | `mvn install -DskipTests`         | `./gradlew build -x test`     |

## 🔹 Performance

Gradle significantly outperforms Maven in large builds due to:
- **Incremental build** – only runs tasks if inputs/outputs changed.
- **Build cache** – reuse outputs from previous runs.
- **Parallel task execution** – speeds up multi-module builds.

## 🔹 Follow-up Interview Questions and Answers

### 1. **Q: Why is Gradle faster than Maven?**
**A:** Gradle uses incremental builds and a build cache system. It tracks the inputs and outputs of tasks, skipping tasks that haven't changed, and allows parallel task execution across modules.

### 2. **Q: Can you migrate a project from Maven to Gradle?**
**A:** Yes, Gradle supports importing Maven projects. You can run:
```bash
gradle init --type pom
```
This generates a `build.gradle` from a `pom.xml`.

### 3. **Q: How does Gradle handle multi-module builds better than Maven?**
**A:** Gradle supports fine-grained configuration across modules, lazy evaluation of tasks, and parallel execution. It also allows cross-module dependency substitution, enabling better isolation.

### 4. **Q: What are the challenges in using Gradle over Maven?**
**A:** Gradle's flexibility can lead to complex build scripts if not managed properly. Teams unfamiliar with Groovy/Kotlin might face a learning curve.

### 5. **Q: Is there any scenario where Maven outperforms Gradle?**
**A:** In very small or legacy projects, Maven's convention-based setup can be quicker to configure and maintain, especially when the build logic is simple.

## 🔹 Summary

| Aspect                | Maven              | Gradle             |
|-----------------------|--------------------|--------------------|
| Verbosity             | More               | Less               |
| Customization         | Harder             | Easier             |
| Speed                 | Slower             | Faster             |
| Learning Curve        | Easier             | Slightly higher    |
| Android Support       | Limited            | Native             |

## 🧠 Tip for Interviews

- If you're asked to pick one, explain **trade-offs** rather than giving a one-sided answer.
- Mention that many enterprise projects still use **Maven**, but new/Android/microservice projects often choose **Gradle** for performance.

-

✅ 5. Best Practices
* Use Maven for stability and standard structure
* Use Gradle for flexibility and faster builds
* Lock dependency versions (dependency locking or BOM)
* Avoid unnecessary transitive dependencies
* Use the wrapper for consistent builds across teams