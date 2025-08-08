package com.genir.aitweaks.launcher

import org.lwjgl.opengl.GL11

/** OpenGL methods are called via the launcher JAR, allowing the starsector-render
 * mod to intercept and modify them. If the methods were placed in the core JAR
 * (loaded by a custom class loader), starsector-render would no longer have access
 * to those OpenGL calls. */
class OpenGL {
    companion object {
        fun glEnable(cap: Int) {
            GL11.glEnable(cap)
        }

        fun glDisable(cap: Int) {
            GL11.glDisable(cap)
        }

        fun glBegin(mode: Int) {
            GL11.glBegin(mode)
        }

        fun glEnd() {
            GL11.glEnd()
        }

        fun glPushAttrib(mask: Int) {
            GL11.glPushAttrib(mask)
        }

        fun glPopAttrib() {
            GL11.glPopAttrib()
        }

        fun glBlendFunc(sfactor: Int, dfactor: Int) {
            GL11.glBlendFunc(sfactor, dfactor)
        }

        fun glLineWidth(width: Float) {
            GL11.glLineWidth(width)
        }

        fun glVertex2f(x: Float, y: Float) {
            GL11.glVertex2f(x, y)
        }
    }
}
