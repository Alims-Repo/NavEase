package io.github.alimsrepo.navease.runtime.registry

// iOS auto-initialization for @AutoRegister screens:
//
// The KSP-generated navEaseBootstrap() function must be called once from your platform
// entry point before the first NavEaseHost() composition:
//
//   fun MainViewController() = ComposeUIViewController {
//       // navEaseBootstrap()  ← no longer needed!
//       App()
//   }
//
// On JVM / Android / Desktop, initialization is automatic via Class.forName.
// On JS / WasmJS, initialization is automatic via module-level init.
//       navEaseBootstrap()



