package io.github.alimsrepo.navease.runtime.data

// ---------------------------------------------------------------------------
// MOVED — backWithResult() and resultOf() have moved to
//   io.github.alimsrepo.navease.runtime.navigation.NavControllerExtensions
//
// These extension functions cannot be re-exported here as deprecated wrappers
// because the typealias NavController = navigation.NavController resolves to
// the same JVM receiver and would create a duplicate method signature error.
//
// Update your imports:
//   import io.github.alimsrepo.navease.runtime.navigation.backWithResult
//   import io.github.alimsrepo.navease.runtime.navigation.resultOf
// ---------------------------------------------------------------------------
