# Ren'Py Support for JetBrains IDEs

## Detailed product and implementation plan for a PyCharm / IntelliJ Platform plugin

**Document type:** Technical implementation plan\
**Target IDE:** PyCharm first; IntelliJ IDEA where compatible\
**Primary language:** Kotlin\
**Platform:** IntelliJ Platform Plugin SDK\
**Reference implementation:** [Ren'Py Magic for VS
Code](https://github.com/adiffx/renpy-magic)\
**Status:** Proposed plan

------------------------------------------------------------------------

# 1. Executive summary

Build a JetBrains plugin that makes Ren'Py development comfortable
inside PyCharm. It should support `.rpy` and `.rpym` files, understand
the important parts of Ren'Py syntax, and provide IDE features such as:

-   syntax highlighting;
-   code completion;
-   navigation to labels, screens, images and transforms;
-   find usages and rename;
-   inspections for unresolved references;
-   documentation on hover;
-   image and audio asset resolution;
-   running Ren'Py lint and launching the game from the IDE.

The goal is not to reimplement the Ren'Py engine. Ren'Py itself remains
responsible for compiling, linting and running the game. The plugin
provides editor intelligence and convenient integration.

The first release should focus on a reliable language model and
navigation. Advanced features such as a label graph, image previews,
audio playback and sophisticated refactoring should follow after the
core language support is stable.

## Recommended architectural decision

Implement the plugin as a **native IntelliJ Platform language plugin in
Kotlin**.

Use the platform's language infrastructure:

-   `Language` and `FileType` for `.rpy` and `.rpym`;
-   lexer and parser for Ren'Py source;
-   PSI (Program Structure Interface) for semantic elements;
-   references and resolve for navigation;
-   completion contributors for context-aware suggestions;
-   inspections for diagnostics;
-   run configurations and external-process integration for Ren'Py SDK
    commands.

This is the most direct route to native JetBrains editor integration.
The IntelliJ Platform's PSI model is the foundation for navigation,
completion, inspections, find usages and refactoring.

### Alternative: an LSP-based implementation

Ren'Py Magic uses a language server. A JetBrains plugin could
potentially act as an LSP client and reuse a separate Ren'Py language
server.

Advantages: - language logic can be shared with VS Code; - language
server can be tested independently; - easier to support multiple
editors.

Disadvantages: - JetBrains LSP integration and APIs must be checked
against the intended IDE versions and distribution model; - native PSI
integration is still valuable for JetBrains-specific refactoring and
editor features; - introducing an LSP process adds packaging, lifecycle
and debugging complexity.

**Recommendation:** start native. Keep language analysis in
well-separated modules so a future language server can reuse the parser
and semantic model. Do not build both implementations in the MVP.

------------------------------------------------------------------------

# 2. Product scope

## 2.1 Target users

-   Ren'Py game developers using PyCharm or IntelliJ IDEA.
-   Developers who write dialogue and branching narrative scripts.
-   Developers who mix Ren'Py script, embedded Python, screens and ATL.
-   Developers who want to keep story documents, game code and assets in
    one project.

## 2.2 Supported file types

  -----------------------------------------------------------------------
  Extension               Purpose                 Initial support
  ----------------------- ----------------------- -----------------------
  `.rpy`                  Main Ren'Py script      Full
                          files                   

  `.rpym`                 Ren'Py Python/module    Full, with
                          files                   Python-oriented
                                                  handling where
                                                  appropriate

  `.rpyc`                 Compiled Ren'Py files   No editing support;
                                                  treat as binary

  `.md`                   Story and design        Existing Markdown
                          documentation           support; no custom
                                                  language needed

  `.py`                   External Python modules Delegate to PyCharm's
                                                  Python support
  -----------------------------------------------------------------------

The plugin should never attempt to treat Markdown as executable Ren'Py
code. Markdown is useful for story outlines, character notes and scene
drafts, while `.rpy` files contain the executable game.

## 2.3 Feature priorities

### MVP --- first usable release

1.  Recognise `.rpy` and `.rpym`.
2.  Syntax highlighting for common Ren'Py constructs.
3.  Correct handling of comments, strings, dialogue and indentation.
4.  Basic parsing of labels, screens, transforms, image definitions and
    common references.
5.  Structure view / file symbols.
6.  Go to definition for labels and screens.
7.  Completion for Ren'Py statements and project labels.
8.  Basic inspections for unresolved `jump`, `call`, `show screen` and
    `use` references.
9.  Plugin settings for the Ren'Py SDK path.
10. Run Ren'Py lint from the IDE.

### Release 2 --- productive editing

1.  Find usages for labels, screens and image definitions.
2.  Rename labels and screens safely.
3.  Completion for images, transforms, styles and transitions.
4.  Hover documentation for Ren'Py keywords and API symbols.
5.  Asset existence checks for images, audio and video.
6.  Gutter navigation icons for labels and references.
7.  Configurable lint on save.
8.  Project-level Ren'Py SDK detection.

### Release 3 --- richer Ren'Py intelligence

1.  ATL syntax and property completion.
2.  Screen language and style-property completion.
3.  Embedded Python support that cooperates with PyCharm's Python
    plugin.
4.  Image preview on hover.
5.  Audio/video asset preview or open action.
6.  Label call hierarchy.
7.  Label flow graph.
8.  Parameter information for common Ren'Py APIs.
9.  Quick fixes and intention actions.

### Later / optional

-   Ren'Py project wizard.
-   Run configurations for projects and scripts.
-   Build/distribution actions.
-   Asset browser and project asset index.
-   Dialogue statistics and word counts.
-   Unreachable-label analysis.
-   Story/choice graph export.
-   Support for additional Ren'Py versions and custom extensions.

------------------------------------------------------------------------

# 3. Reference feature analysis: Ren'Py Magic

The reference project is useful as a product specification. Its feature
set includes:

  -----------------------------------------------------------------------
  Area                    Ren'Py Magic capability JetBrains
                                                  implementation
  ----------------------- ----------------------- -----------------------
  Syntax                  `.rpy`, `.rpym`, ATL,   Lexer, parser, syntax
                          embedded Python,        highlighter
                          dialogue tags           

  Documentation           Hover for Ren'Py API    Documentation provider
                          and properties          

  Images                  Preview referenced      PSI reference + editor
                          images                  hover/preview

  Audio/video             Resolve and open media  Asset resolver +
                          assets                  open/play action

  Navigation              Labels, screens,        PSI references and
                          transforms, images,     symbol indexes
                          styles                  

  Workspace search        Search symbols across   Find Symbols / project
                          scripts                 indexes

  Label graph             Visualise label flow    Tool window or editor
                                                  tab

  Call hierarchy          Incoming/outgoing label Call hierarchy provider
                          calls                   or custom tool window

  Refactoring             Rename and find usages  `PsiNamedElement`,
                                                  references and rename
                                                  support

  Completion              Keywords, labels,       Completion contributors
                          screens, images,        
                          properties              

  Signature help          Function/API parameter  Parameter info handler
                          hints                   

  Diagnostics             Undefined               Inspections + parser
                          labels/screens, quote   errors
                          errors                  

  Native lint             Run Ren'Py lint         External process
                                                  integration
  -----------------------------------------------------------------------

The reference project's public README describes the features and
limitations. Its implementation uses a language server, so its source
code should be treated as a functional reference, not copied directly
into the JetBrains plugin.

Reference links: - Repository: https://github.com/adiffx/renpy-magic -
Marketplace:
https://marketplace.visualstudio.com/items?itemName=adiffx.renpy-language-support

Before implementation, inspect the repository's licence and individual
dependencies. Reuse of code, grammar data or generated API data must
comply with their respective licences and attribution requirements.

------------------------------------------------------------------------

# 4. Technical architecture

## 4.1 High-level components

``` text
PyCharm / IntelliJ IDEA
        |
        v
RenPy Plugin
  |
  +-- File type and language registration
  |
  +-- Lexer
  |     +-- Ren'Py tokens
  |     +-- Python block boundaries
  |     +-- Strings and dialogue
  |     +-- Comments and indentation
  |
  +-- Parser
  |     +-- Statements
  |     +-- Labels and control flow
  |     +-- Screens and styles
  |     +-- Images and transforms
  |     +-- ATL and menus
  |
  +-- PSI / semantic model
  |     +-- Definitions
  |     +-- References
  |     +-- Symbol indexes
  |     +-- Scope and local labels
  |
  +-- Editor features
  |     +-- Highlighting
  |     +-- Completion
  |     +-- Navigation
  |     +-- Inspections
  |     +-- Documentation
  |     +-- Refactoring
  |
  +-- Ren'Py SDK integration
        +-- SDK detection
        +-- Lint
        +-- Launch game
        +-- Build commands (later)
```

Keep the Ren'Py language implementation separate from SDK process
integration. Editor features should continue to work even if no Ren'Py
SDK is configured.

## 4.2 Suggested technology stack

  Concern                    Suggested technology
  -------------------------- -------------------------------------------------
  Implementation             Kotlin
  Build                      Gradle
  Plugin build conventions   IntelliJ Platform Gradle Plugin
  IDE platform               IntelliJ Platform SDK
  Language model             Lexer + Parser + PSI
  Lexer generation           JFlex, if useful
  Parser generation          Grammar-Kit, if grammar complexity justifies it
  Tests                      IntelliJ Platform test framework, Kotlin/JUnit
  Static analysis            Kotlin inspections, Gradle checks
  CI                         GitHub Actions or Azure Pipelines
  Distribution               JetBrains Marketplace ZIP

Use the current JetBrains plugin template and current SDK documentation
when creating the project. Pin a supported IntelliJ Platform baseline
and test against the intended PyCharm versions. Avoid choosing an old
SDK version based on an outdated tutorial.

## 4.3 Compatibility strategy

Start with PyCharm as the primary target.

Decide whether the first release targets: - PyCharm only; or - multiple
IntelliJ Platform IDEs, with Python-specific functionality enabled only
when the Python plugin is present.

A Ren'Py language plugin does not inherently need the Python plugin for
basic `.rpy` syntax support. However, deeper embedded Python support
should integrate with PyCharm's Python language facilities when
available.

Declare only the platform and optional plugin dependencies actually
required. Test compatibility against the exact PyCharm release range
declared in plugin metadata.

------------------------------------------------------------------------

# 5. Ren'Py language model and parsing strategy

This is the most important and technically risky part of the project.

Ren'Py is not simply Python with a few extra keywords. It has its own
statement syntax, indentation rules, dialogue forms, screen language,
ATL, and embedded Python blocks.

A naive line-based parser will fail on realistic projects.

## 5.1 Start with a deliberately limited grammar

Initially recognise these constructs:

-   `label`
-   `jump`
-   `call`
-   `return`
-   `scene`
-   `show`
-   `hide`
-   `with`
-   `menu`
-   `if`, `elif`, `else`
-   `while`, `for`
-   `define`
-   `default`
-   `image`
-   `transform`
-   `screen`
-   `use`
-   `python`
-   `init`
-   `play`, `queue`, `stop`
-   dialogue and narration
-   comments and strings

Add more syntax incrementally. Unknown syntax should be represented as a
generic or incomplete statement rather than causing the entire file to
fail parsing.

## 5.2 Lexer design

The lexer must identify tokens such as:

-   Ren'Py keywords;
-   identifiers;
-   labels and dotted local-label names;
-   strings, including single, double and triple-quoted strings;
-   comments;
-   indentation and newlines;
-   operators and punctuation;
-   dialogue speaker names;
-   embedded Python regions;
-   text interpolation and Ren'Py text tags.

Important edge cases:

-   quoted dialogue may contain punctuation and Ren'Py text tags;
-   strings can span multiple lines;
-   Python blocks are indentation-sensitive;
-   comments and strings must not accidentally be interpreted as Ren'Py
    statements;
-   screen language and ATL have their own property-like syntax;
-   Ren'Py syntax may evolve between engine releases.

Use a lexer state machine where necessary. Do not assume that every line
can be classified independently.

## 5.3 Embedded Python

Embedded Python is a major source of complexity.

Examples include:

``` renpy
$ score += 1
```

``` renpy
python:
    score += 1
    if score > 5:
        persistent.unlocked = True
```

``` renpy
init python:
    def calculate_score(value):
        return value * 10
```

Recommended approach:

1.  The Ren'Py lexer identifies Python regions and their boundaries.
2.  The Ren'Py parser stores the Python region as a dedicated PSI node.
3.  Where possible, expose the embedded Python text to PyCharm's Python
    language support.
4.  If full language injection is too complex initially, provide correct
    highlighting boundaries and basic structural support first.

Do not write a complete Python parser inside the Ren'Py plugin.

## 5.4 PSI model

Create PSI elements for meaningful language constructs, for example:

-   `RenPyFile`
-   `RenPyLabel`
-   `RenPyJumpStatement`
-   `RenPyCallStatement`
-   `RenPyScreen`
-   `RenPyUseStatement`
-   `RenPyImageDefinition`
-   `RenPyShowStatement`
-   `RenPySceneStatement`
-   `RenPyTransform`
-   `RenPyMenu`
-   `RenPyPythonBlock`
-   `RenPyDialogue`

The exact class names are implementation choices. The key is that
definitions and references should be represented consistently.

Definitions that support rename and find usages should expose names
through the platform's named-element interfaces and references.

## 5.5 Scope and local labels

Ren'Py local labels require special treatment.

For example:

``` renpy
label chapter_one:
    jump .next_scene

    label .next_scene:
        "The next scene."
        return
```

The local label `.next_scene` is not a globally independent label. Its
resolution depends on the enclosing global label and Ren'Py's scoping
rules.

Implement this in a dedicated resolver and add tests for: - local label
inside a global label; - local label references from the same global
label; - duplicate local names in different global labels; - references
to missing local labels; - local labels across included script files,
according to actual Ren'Py rules.

Do not resolve local labels by simple project-wide name matching.

------------------------------------------------------------------------

# 6. Semantic analysis and project indexing

The plugin should build a lightweight symbol model from project files.

## 6.1 Initial symbol types

  --------------------------------------------------------------------------
  Symbol                  Definition              References
  ----------------------- ----------------------- --------------------------
  Global label            `label intro:`          `jump intro`, `call intro`

  Local label             `label .next:`          `jump .next`, `call .next`

  Screen                  `screen inventory:`     `show screen inventory`,
                                                  `call screen inventory`,
                                                  `use inventory`

  Transform               `transform left:`       `at left`

  Image                   `image bg room = ...`   `scene bg room`,
                                                  `show bg room`

  Style                   `style dialogue:`       style references

  Variable                `define`, `default`,    Context-dependent
                          selected assignments    references
  --------------------------------------------------------------------------

Image handling needs special care because Ren'Py can infer images from
files and image names can contain multiple space-separated components.
Do not assume every image is explicitly declared with an `image`
statement.

## 6.2 Indexing

Use IntelliJ Platform indexing mechanisms rather than rescanning every
script on each keystroke.

The index should support: - symbol lookup by name; - project-wide label
and screen completion; - navigation to definitions; - find usages; -
duplicate-definition checks.

Respect dumb mode and platform indexing constraints. Editor features
that require indexes should degrade gracefully while indexing is in
progress.

## 6.3 Asset index

Build a project asset index for supported file types:

-   images: PNG, JPG/JPEG, WEBP and formats commonly used by Ren'Py;
-   audio: OGG, MP3, WAV and other supported formats;
-   video: formats supported by the selected Ren'Py version.

The asset resolver should: 1. discover files under the project's game
directory; 2. map paths to Ren'Py asset names according to documented
engine conventions; 3. resolve string-based paths and code-defined
names; 4. provide completion and diagnostics; 5. avoid warning on
dynamic paths that cannot be statically resolved.

Do not make undefined-image warnings noisy by default. Many projects use
file-derived image names and dynamic construction.

------------------------------------------------------------------------

# 7. Editor features in detail

## 7.1 Syntax highlighting

Create a colour scheme page with separate categories for:

-   Ren'Py keywords;
-   labels;
-   dialogue speaker names;
-   dialogue text;
-   narration;
-   strings;
-   comments;
-   numbers;
-   operators;
-   embedded Python;
-   ATL keywords and properties;
-   screen language;
-   Ren'Py text tags;
-   interpolation expressions.

Provide a useful default scheme, but let users customise colours through
the standard JetBrains settings.

## 7.2 Code completion

Implement completion in stages.

### Stage 1: statement completion

Examples: - `label` - `jump` - `call` - `scene` - `show` - `hide` -
`menu` - `screen` - `transform` - `define` - `default`

### Stage 2: project symbols

-   global labels after `jump` and `call`;
-   screens after `show screen`, `call screen` and `use`;
-   transforms after `at`;
-   image names after `scene`, `show` and `hide`.

### Stage 3: contextual properties

-   transitions after `with`;
-   audio names after `play` and `queue`;
-   ATL properties;
-   screen properties;
-   style properties;
-   image attributes and clauses.

Completion must use the parser's current context. Avoid suggesting every
Ren'Py keyword in every position.

## 7.3 Navigation

Implement: - Go to Definition for labels; - Go to Definition for
screens; - Go to Definition for transforms; - Go to Definition for
code-defined images; - navigation from `jump`, `call`, `show screen`,
`call screen` and `use`; - file structure view; - project-wide symbol
search.

Navigation should work across multiple `.rpy` and `.rpym` files.

## 7.4 Find usages and rename

Implement only after references resolve reliably.

Rename should: - update the definition; - update statically resolved
references; - preserve local-label scope; - avoid replacing unrelated
text or strings; - show a preview where appropriate.

Do not offer rename for ambiguous or dynamically constructed references.

## 7.5 Inspections and diagnostics

Initial inspections:

  ------------------------------------------------------------------------
  Inspection              Severity                Example
  ----------------------- ----------------------- ------------------------
  Undefined global label  Warning                 `jump missing_label`

  Undefined local label   Warning                 `jump .missing`

  Undefined screen        Warning                 `use missing_screen`

  Duplicate global label  Error/warning           Two `label intro:`
                                                  definitions

  Duplicate screen        Error/warning           Two `screen inventory:`
                                                  definitions

  Unresolved transform    Warning                 `at missing_transform`

  Missing static asset    Warning                 Referenced image/audio
                                                  file does not exist

  Invalid or incomplete   Parser error            Incomplete statement
  syntax                                          
  ------------------------------------------------------------------------

All inspections should have sensible suppression options and avoid false
positives.

## 7.6 Hover documentation

Provide documentation for: - Ren'Py statements and keywords; - built-in
classes and functions; - `renpy.*` APIs; - common transitions and
displayables; - `config.*`, `gui.*` and `build.*`; - ATL properties; -
screen language properties.

### API data source

Generate or maintain a versioned API documentation dataset from official
Ren'Py documentation or source documentation.

Recommended pipeline: 1. identify the authoritative documentation/source
files for supported Ren'Py releases; 2. write a deterministic extractor;
3. normalise entries into a JSON or generated Kotlin data structure; 4.
record the source Ren'Py version; 5. validate generated output in CI; 6.
include attribution and comply with documentation/source licences.

Do not manually maintain hundreds of API entries if they can be
generated reliably.

## 7.7 Image and media preview

For image references: - resolve the referenced image; - show a preview
on hover; - offer an open-file action; - optionally show dimensions and
file path.

For audio/video: - resolve the asset; - offer an open/play action using
an appropriate platform facility; - do not assume every format can be
played inside the IDE.

These are useful productivity features, but not MVP requirements.

## 7.8 Label graph and call hierarchy

A label graph should be based on resolved control-flow references, not
text matching.

Represent: - labels as nodes; - `jump` as a directed edge; - `call` as a
call edge; - menu choices as labelled branches; - implicit fall-through
where it can be determined reliably.

Possible uses: - inspect chapter structure; - find unreachable labels; -
understand branching routes; - navigate from graph nodes to source.

Build this as a separate tool window or editor tab. Do not make graph
rendering a dependency of the language parser.

------------------------------------------------------------------------

# 8. Ren'Py SDK integration

The plugin should work without an SDK configured, but SDK integration
adds substantial value.

## 8.1 SDK settings

Provide a settings page with:

-   Ren'Py SDK path;
-   detected Ren'Py version;
-   validation status;
-   lint on save;
-   lint before run;
-   optional command timeout;
-   optional environment variables.

Support manual SDK path selection first. Automatic discovery can be
added later.

## 8.2 SDK detection

Validate that the selected directory looks like a valid Ren'Py SDK. Do
not rely on one hard-coded executable name across every operating system
and Ren'Py release.

Encapsulate platform-specific command construction in an SDK service.

Test on: - Windows; - macOS; - Linux.

## 8.3 Run lint

Provide: - **Ren'Py: Run Lint** action; - optional lint on save; -
project-level lint; - parsing of standard output and error output; -
clickable diagnostics where source locations can be mapped.

Important: verify the actual Ren'Py SDK command and output format for
every supported Ren'Py version. Do not assume that a command line copied
from an older release is still valid.

Use background tasks. Do not block the UI thread while lint is running.

## 8.4 Launch the game

Provide an IDE action or run configuration that launches the selected
Ren'Py project through the configured SDK.

Potential configuration fields: - SDK path; - project directory; -
optional arguments; - environment variables; - working directory.

The Ren'Py Launcher remains available as the fallback and authoritative
project-management interface.

## 8.5 Build and distribution

Later, add build actions only after launch and lint are stable. Build
commands and output formats should be verified against the target Ren'Py
version.

------------------------------------------------------------------------

# 9. Suggested repository structure

``` text
renpy-jetbrains/
├── README.md
├── LICENSE
├── CHANGELOG.md
├── CONTRIBUTING.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── docs/
│   ├── architecture.md
│   ├── renpy-language-notes.md
│   ├── feature-roadmap.md
│   └── test-strategy.md
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/example/renpy/
│   │   │       ├── RenPyLanguage.kt
│   │   │       ├── RenPyFileType.kt
│   │   │       ├── lexer/
│   │   │       ├── parser/
│   │   │       ├── psi/
│   │   │       ├── references/
│   │   │       ├── index/
│   │   │       ├── completion/
│   │   │       ├── inspections/
│   │   │       ├── documentation/
│   │   │       ├── highlighting/
│   │   │       ├── sdk/
│   │   │       ├── run/
│   │   │       └── ui/
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml
│   │       └── icons/
│   └── test/
│       ├── kotlin/
│       └── testData/
│           ├── lexer/
│           ├── parser/
│           ├── references/
│           ├── completion/
│           ├── inspections/
│           └── sdk/
└── .github/
    └── workflows/
        └── build.yml
```

Replace `com/example/renpy` with the final organisation/package
namespace.

Keep generated grammar and API data in clearly identified locations.
Avoid mixing generated files with handwritten PSI and resolver code.

------------------------------------------------------------------------

# 10. Development milestones

The following is a realistic staged plan for one experienced developer
working part-time. Actual duration depends heavily on Ren'Py grammar
coverage and how much testing is done against real projects.

## Milestone 0 --- research and proof of concept

**Goal:** confirm the JetBrains approach before committing to a full
parser.

Tasks: - inspect Ren'Py Magic features and source organisation; - review
its licence; - review current IntelliJ Platform language-plugin
documentation; - create a Kotlin IntelliJ Platform plugin project; -
register `.rpy` and `.rpym` file types; - create a minimal
lexer/highlighter; - run the plugin in an IDE development instance.

Acceptance criteria: - plugin loads in the target PyCharm version; -
`.rpy` and `.rpym` are recognised; - basic keywords, comments and
strings are highlighted; - plugin can be built and tested from Gradle.

## Milestone 1 --- lexer and parser foundation

**Goal:** reliably recognise the core language constructs.

Tasks: - define token types; - implement indentation-aware
tokenisation; - support strings, comments and dialogue; - recognise
labels and common statements; - represent embedded Python blocks; - add
parser recovery for incomplete files; - build lexer and parser fixture
tests.

Acceptance criteria: - representative scripts parse without losing
source text; - malformed/incomplete code does not break the whole
file; - editing a file does not cause excessive reparsing or UI freezes.

## Milestone 2 --- PSI and navigation

**Goal:** make the project navigable.

Tasks: - implement PSI nodes for labels, screens, transforms and
images; - implement named elements; - create references for jump/call
and screen usage; - resolve global and local labels; - add file
structure view; - add workspace symbol indexing.

Acceptance criteria: - Go to Definition works across files; - local
labels resolve in their correct scope; - symbol search finds labels and
screens; - duplicate definitions can be detected.

## Milestone 3 --- completion and inspections

**Goal:** deliver the first genuinely useful developer experience.

Tasks: - statement keyword completion; - label and screen completion; -
transform completion; - undefined label/screen inspections; - duplicate
symbol inspections; - parser error presentation; - inspection tests.

Acceptance criteria: - completion is context-sensitive; - inspections do
not produce excessive false positives; - tests cover valid and invalid
projects.

## Milestone 4 --- Ren'Py SDK integration

**Goal:** make the plugin useful without leaving the IDE for common
checks.

Tasks: - SDK settings page; - SDK path validation; - Ren'Py version
detection; - background lint process; - parse lint output; - clickable
diagnostics; - launch project action.

Acceptance criteria: - lint runs on a sample project; - output appears
in an IDE tool window or Problems view; - source diagnostics navigate to
the correct file and line; - IDE remains responsive while commands run.

## Milestone 5 --- productive language features

**Goal:** approach the core productivity of Ren'Py Magic.

Tasks: - find usages; - safe rename for labels/screens; - image and
asset resolver; - image completion; - audio completion; - hover
documentation; - API data generation.

Acceptance criteria: - rename updates only resolved references; - assets
resolve according to tested Ren'Py conventions; - API documentation is
versioned and reproducible.

## Milestone 6 --- advanced features

**Goal:** differentiate the plugin and improve larger-project workflows.

Tasks: - ATL and screen-language completion; - embedded Python
integration; - image preview; - audio/video open/play; - call
hierarchy; - label graph; - unreachable-label inspection; - quick fixes.

Acceptance criteria: - each feature has tests and documented
limitations; - large projects remain responsive; - features degrade
gracefully when references are dynamic or ambiguous.

## Milestone 7 --- release and maintenance

Tasks: - test supported PyCharm versions; - test Windows, macOS and
Linux where feasible; - verify plugin verifier output; - document SDK
configuration; - publish a Marketplace listing; - create release
notes; - establish issue templates and contribution guidelines; -
automate CI builds and compatibility checks.

Acceptance criteria: - reproducible release build; - no blocking
verifier issues; - documented compatibility matrix; - sample project
passes all plugin tests.

------------------------------------------------------------------------

# 11. Testing strategy

A parser and resolver need a representative corpus. Do not rely on a
handful of hand-written examples.

## 11.1 Test fixtures

Create small fixtures for: - narration and dialogue; - speaker
definitions; - labels and local labels; - jump and call; - menus and
nested choices; - if/elif/else; - image definitions and inferred image
names; - scene/show/hide; - screens and `use`; - transforms and ATL; -
embedded Python; - init and default statements; - triple-quoted
strings; - Ren'Py text tags and interpolation; - comments and
malformed/incomplete code.

## 11.2 Real project testing

Use a small synthetic project first. Then test against projects that are
legally available for testing.

Do not commit commercial game scripts or assets to the plugin repository
without permission. If testing against a user's own project, keep test
data private and remove personal or proprietary content from bug
reports.

## 11.3 Test layers

  Test layer              What it verifies
  ----------------------- -----------------------------------------------------
  Lexer tests             Correct tokenisation and state transitions
  Parser tests            Correct PSI structure and error recovery
  Resolver tests          Definitions and references resolve correctly
  Completion tests        Suggestions appear in the correct context
  Inspection tests        Correct warnings and no obvious false positives
  SDK integration tests   Command execution and output parsing
  UI tests                Actions, settings and tool windows behave correctly
  Compatibility tests     Plugin loads in declared IDE versions

## 11.4 Performance tests

Test at least: - a small project with a few scripts; - a medium project
with hundreds of scripts/assets; - a large synthetic project with
thousands of symbols.

Measure: - indexing time; - completion latency; - typing
responsiveness; - memory usage; - lint duration; - asset-index refresh
time.

Do not run a full-project scan on every keystroke. Use indexes,
incremental updates, cancellation and background tasks.

------------------------------------------------------------------------

# 12. Risks and mitigations

  -----------------------------------------------------------------------
  Risk                    Why it matters          Mitigation
  ----------------------- ----------------------- -----------------------
  Ren'Py grammar          It mixes several        Start with a limited
  complexity              language forms          grammar; add fixtures
                                                  continuously

  Embedded Python         Python blocks have      Delegate to PyCharm
                          their own syntax and    Python support where
                          indentation             possible

  Dynamic image names     Static analysis cannot  Make diagnostics
                          resolve every image     conservative and
                                                  configurable

  Local-label scope       Naive resolution        Implement explicit
                          creates false results   scope rules and tests

  Ren'Py version          Syntax and SDK commands Version the grammar/API
  differences             can change              data and test multiple
                                                  SDK releases

  PSI performance         Large projects can      Use indexes,
                          become slow             incremental updates and
                                                  background work

  JetBrains API changes   Platform APIs evolve    Pin a baseline and run
                                                  compatibility checks

  Plugin compatibility    PyCharm                 Declare tested IDE
                          editions/releases       ranges and optional
                          differ                  dependencies

  Lint output changes     External command output Isolate command
                          can vary                execution and output
                                                  parsing

  Licence issues          Reference               Review before copying
                          implementation and docs or generating data
                          have licences           
  -----------------------------------------------------------------------

------------------------------------------------------------------------

# 13. Suggested implementation order

Avoid implementing all features at once.

Recommended order:

1.  Plugin skeleton and `.rpy` file type.
2.  Syntax highlighting.
3.  Lexer and parser for core statements.
4.  PSI for labels and screens.
5.  Go to Definition.
6.  Label and screen completion.
7.  Undefined-reference inspections.
8.  Ren'Py SDK detection and lint.
9.  Asset completion and validation.
10. Documentation, rename and find usages.
11. ATL, screen language and embedded Python improvements.
12. Graphs, previews and advanced tooling.

This order gives a useful plugin early and reduces the risk of spending
months on advanced features before the language foundation works.

------------------------------------------------------------------------

# 14. Definition of a successful MVP

The first public MVP is successful when a developer can:

-   open a Ren'Py project in PyCharm;
-   edit `.rpy` and `.rpym` files with readable syntax highlighting;
-   see labels and screens in the file structure;
-   navigate from `jump` and `call` to label definitions;
-   get label and screen completion;
-   see warnings for unresolved labels and screens;
-   configure a Ren'Py SDK;
-   run Ren'Py lint without leaving the IDE.

Do not make image previews, label graphs, full Python integration or
complete ATL support release blockers.

------------------------------------------------------------------------

# 15. Initial project setup checklist

-   [ ] Install or enable the JetBrains Plugin DevKit.
-   [ ] Install Grammar-Kit if using a generated parser.
-   [ ] Create a Kotlin IntelliJ Platform plugin project from the
    current official template.
-   [ ] Select the minimum PyCharm/IntelliJ Platform version to support.
-   [ ] Add `.rpy` and `.rpym` file type registration.
-   [ ] Add a minimal syntax highlighter.
-   [ ] Add a plugin run configuration for the IDE development instance.
-   [ ] Add lexer tests.
-   [ ] Add a tiny Ren'Py fixture project.
-   [ ] Set up CI to build and test the plugin.
-   [ ] Record supported Ren'Py and PyCharm versions in the README.

------------------------------------------------------------------------

# 16. Official technical references

Use these as the authoritative starting points. APIs and recommended
Gradle configuration can change, so consult the current documentation
when implementing.

## JetBrains

-   [IntelliJ Platform Plugin
    SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
-   [Custom Language
    Support](https://plugins.jetbrains.com/docs/intellij/custom-language-support.html)
-   [Implementing a
    Lexer](https://plugins.jetbrains.com/docs/intellij/implementing-lexer.html)
-   [Implementing Parser and
    PSI](https://plugins.jetbrains.com/docs/intellij/implementing-parser-and-psi.html)
-   [Syntax and Error
    Highlighting](https://plugins.jetbrains.com/docs/intellij/syntax-highlighting-and-error-highlighting.html)
-   [References and
    Resolve](https://plugins.jetbrains.com/docs/intellij/psi-references.html)
-   [Code
    Completion](https://plugins.jetbrains.com/docs/intellij/code-completion.html)
-   [Plugin
    Compatibility](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html)
-   [Plugin
    Verifier](https://plugins.jetbrains.com/docs/intellij/verifying-plugin-compatibility.html)
-   [Plugin
    Template](https://github.com/JetBrains/intellij-platform-plugin-template)

## Ren'Py

-   [Ren'Py Documentation](https://www.renpy.org/doc/html/)
-   [Language
    Basics](https://www.renpy.org/doc/html/language_basics.html)
-   [Python and Ren'Py](https://www.renpy.org/doc/html/python.html)
-   [Screens](https://www.renpy.org/doc/html/screens.html)
-   [ATL](https://www.renpy.org/doc/html/atl.html)
-   [Developer Tools and
    Lint](https://www.renpy.org/doc/html/developer_tools.html)

## Reference project

-   [Ren'Py Magic source
    repository](https://github.com/adiffx/renpy-magic)
-   [Ren'Py Magic feature
    listing](https://marketplace.visualstudio.com/items?itemName=adiffx.renpy-language-support)

------------------------------------------------------------------------

# 17. Final recommendation

Build a **Kotlin-based native IntelliJ Platform plugin**, with PyCharm
as the first supported IDE.

The first technical milestone should be deliberately small: register
`.rpy` files, highlight a few core statements, parse global labels, and
navigate from `jump` to a label definition. Once that works reliably,
add screens, local labels, completion and inspections.

Treat Ren'Py's own SDK as the authority for linting and execution. Treat
Markdown as project documentation, not as the executable game language.
Keep the plugin's parser, semantic model, editor features and SDK
integration separated so the project can grow without becoming one large
collection of editor callbacks.

**The most important success factor is not the number of features. It is
a correct, testable understanding of Ren'Py syntax and symbol
resolution.**
