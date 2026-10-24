package io.redigit.skimbyte.util

// No-op passthrough until a canvas-based crop is wired for the web (Wasm) target.
actual suspend fun cropToAspect(image: PickedImage, targetAspect: Float): PickedImage = image
