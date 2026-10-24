package io.redigit.skimbyte.util

// No-op passthrough until an iOS image codec (UIKit/CoreGraphics) crop is wired here. The server
// stores whatever is uploaded, so an uncropped image is still valid - it just isn't trimmed to the
// target aspect on iOS yet.
actual suspend fun cropToAspect(image: PickedImage, targetAspect: Float): PickedImage = image
