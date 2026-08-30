package com.gudu0;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

@SuppressWarnings("unused")
public class Input {

    public enum ActionType {
        Release(GLFW_RELEASE), // 0
        Press(GLFW_PRESS),     // 1
        Repeat(GLFW_REPEAT),   // 2
        Unknown(-1);

        private final int value;
        ActionType(int value) { this.value = value; }
        public int getValue() { return this.value; }

        // Fast helper to convert GLFW's primitive integer action to our Enum
        public static ActionType fromGlfw(int action) {
            return switch (action) {
                case GLFW_RELEASE -> Release;
                case GLFW_PRESS -> Press;
                case GLFW_REPEAT -> Repeat;
                default -> Unknown;
            };
        }
    }

    private final ActionType[] lastKeys = new ActionType[GLFW_KEY_LAST + 1];
    private final ActionType[] keys = new ActionType[GLFW_KEY_LAST + 1];
    private final ActionType[] mouseButtons = new ActionType[GLFW_MOUSE_BUTTON_LAST + 1];
    private final ActionType[] lastButtons = new ActionType[GLFW_MOUSE_BUTTON_LAST + 1];
    private double mouseX, mouseY;


    public Input() {
        // Fill the arrays so they do not contain null elements
        Arrays.fill(keys, ActionType.Release);
        Arrays.fill(lastKeys, ActionType.Release);
        Arrays.fill(mouseButtons, ActionType.Release);
        Arrays.fill(lastButtons, ActionType.Release);
    }

    public void update() {
        System.arraycopy(keys, 0, lastKeys, 0, keys.length);
        System.arraycopy(mouseButtons, 0, lastButtons, 0, mouseButtons.length);
    }

    // --- Getters for Game Loop Queries ---

    public boolean isKeyPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) return false;
        ActionType state = keys[keyCode];
        // Handle null safety + allow repeating actions to count as "pressed"
        return state == ActionType.Press || state == ActionType.Repeat;
    }
    public boolean isKeyJustPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) return false;

        boolean isCurrentPress = keys[keyCode] == ActionType.Press;
        boolean wasPreviouslyPress = lastKeys[keyCode] == ActionType.Press || lastKeys[keyCode] == ActionType.Repeat;

        return isCurrentPress && !wasPreviouslyPress;
    }
    public boolean isMouseButtonPressed(int buttonCode) {
        if (buttonCode < 0 || buttonCode >= mouseButtons.length) return false;
        ActionType state = mouseButtons[buttonCode];
        return state == ActionType.Press; // Mouse buttons do not repeat in GLFW
    }
    public boolean isMouseButtonJustPressed(int buttonCode) {
        if (buttonCode < 0 || buttonCode >= mouseButtons.length) return false;
        boolean isCurrentPress = mouseButtons[buttonCode] == ActionType.Press;
        boolean wasPreviouslyPress = lastButtons[buttonCode] == ActionType.Press;

        return isCurrentPress && !wasPreviouslyPress;
    }

    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }

    // --- GLFW Callback Setups ---

    public void setupCallbacks(long windowHandle) {
        // Setup Keyboard Callback
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
//            System.out.println("Native GLFW Key Event: " + key + " Action: " + action);

            // Handle engine-level shortcuts like ESCAPE closing the window
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true);
                Main.window_close_callback(window, "escape keypress");
            }

            // Update your state arrays
            if (key >= 0 && key < keys.length) {
                keys[key] = ActionType.fromGlfw(action);
            }
        });


        // Setup Mouse Button Callback
        glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
//            System.out.println("Native GLFW Mouse Event: " + button + " Action: " + action);
            if (button >= 0 && button < mouseButtons.length) {
                mouseButtons[button] = ActionType.fromGlfw(action);
            }
        });

        // Setup Mouse Position Callback
        glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
//            System.out.println("Native GLFW cursor Event: \nX: " + xpos + " \nY: "  + ypos );
            mouseX = xpos;
            mouseY = ypos;
        });
    }
}
