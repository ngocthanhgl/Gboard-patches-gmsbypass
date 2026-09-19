package dev.jason.gboardpatches.patches.gboard.features.gmsbypass

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal val gboardGmsBypassPatch = bytecodePatch(
    description = "Force skip GMS check (GoogleApiAvailability -> SUCCESS) for Vivo/no-GMS"
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    execute {
        var patched = 0
        for (cls in classes) {
            for (method in cls.methods) {
                val impl = method.implementation ?: continue
                val text = impl.instructions.joinToString(" ") { it.toString() }
                val hasGmsRef = text.contains("GoogleApiAvailability") ||
                    text.contains("SERVICE_MISSING") ||
                    text.contains("isGooglePlayServicesAvailable")
                if (!hasGmsRef) continue
                if (method.returnType == "I") {
                    try {
                        // insert at method start: return 0 (SUCCESS)
                        method.addInstruction(0, "const/4 v0, 0x0")
                        method.addInstruction(1, "return v0")
                        patched++
                    } catch (_: Exception) {}
                }
            }
        }
        println("[GboardGmsBypass] patched $patched methods")
    }
}
