package com.gudu0;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;


import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class Main {

    private static final GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);
    private long windowID;
    private static final int windowWidth = 640;
    private static final int windowHeight = 480;
    private float posx = 320f;
    private float posy = 240f;
    private float posr = 0.0f;
    private float movespd = 3.5f;
    private float rotspd = 2.0f;
    private final Color RED = new Color(1.0f, 0.0f, 0.0f);
    private final Color GREEN = new Color(0.0f, 1.0f, 0.0f);
    private final Color BLUE = new Color(0.0f, 0.0f, 1.0f);
    private final Color BLACK = new Color(0.0f, 0.0f, 0.0f);
    private final Color WHITE = new Color(1.0f, 1.0f, 1.0f);
    private final Color ORANGE = new Color(0.949f, 0.396f, 0.086f);
    private final Color YELLOW = new Color(1.0f, 1.0f, 0.0f);
    private final ArrayList<Point> points = new ArrayList<>();

    public static void main(String[] args) {
        Main mainInstance = new Main();
        mainInstance.windowID = mainInstance.initWindow();
        Input input = new Input();
        input.setupCallbacks(mainInstance.windowID);

        //region fps
        long lastTime = System.nanoTime();
        long fpsTimer = System.nanoTime();
        int frameCount = 0;
        //endregion
        while (!glfwWindowShouldClose(mainInstance.windowID)) {
            //region fps
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime - lastTime;
            lastTime = currentTime;
            frameCount++;
            if (System.nanoTime() - fpsTimer >= 1000000000) {
                glfwSetWindowTitle(mainInstance.windowID, "GUDUWORLD | FPS: " + frameCount);
                frameCount = 0;
                fpsTimer = System.nanoTime();
            }
            double delta = elapsedTime / 1000000000.0;
            //endregion

            mainInstance.render(mainInstance);


            input.update();
            glfwPollEvents();


            if (input.isKeyPressed(GLFW_KEY_A)) {
                mainInstance.posx -= mainInstance.movespd;
            } else if (input.isKeyPressed(GLFW_KEY_D)) {
                mainInstance.posx += mainInstance.movespd;
            }
            if (input.isKeyPressed(GLFW_KEY_W)) {
                mainInstance.posy -= mainInstance.movespd;
            }  else if (input.isKeyPressed(GLFW_KEY_S)) {
                mainInstance.posy += mainInstance.movespd;
            }
            if (input.isKeyPressed(GLFW_KEY_Q)) {
                mainInstance.posr -= mainInstance.rotspd;
            }  else if (input.isKeyPressed(GLFW_KEY_E)) {
                mainInstance.posr += mainInstance.rotspd;
            }

            if (input.isMouseButtonPressed(GLFW_MOUSE_BUTTON_1)) {
                mainInstance.points.add(new Point(input.getMouseX(), input.getMouseY()));
            }

            glfwSwapBuffers(mainInstance.windowID);
        }

        glfwDestroyWindow(mainInstance.windowID);
        Callbacks.glfwFreeCallbacks(mainInstance.windowID);
        glfwTerminate();
        errorCallback.free();
    }

    public long initWindow() {
        glfwSetErrorCallback(errorCallback);

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        //region glfwWindowHints
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);
        glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);
        glfwWindowHint(GLFW_FLOATING, GLFW_FALSE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_FALSE);
        glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE);
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);
        glfwWindowHint(GLFW_SCALE_FRAMEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_POSITION_X, GLFW_ANY_POSITION);
        glfwWindowHint(GLFW_POSITION_Y, GLFW_ANY_POSITION);
        glfwWindowHint(GLFW_WIN32_KEYBOARD_MENU, GLFW_FALSE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        //endregion

        long windowID = glfwCreateWindow(Main.windowWidth, Main.windowHeight, "My Window", NULL, NULL);
        if (windowID == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }
        glfwSetWindowCloseCallback(windowID, Main::window_close_callback);
        glfwSetWindowSizeCallback(windowID, (window, width, height) -> setupPerspective(width, height));

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidMode == null) throw new RuntimeException("Failed to retrieve the GLFW video mode");
        glfwSetWindowPos(windowID,
                (vidMode.width() - Main.windowWidth) / 2,
                (vidMode.height() - Main.windowHeight) / 2
        );

        glfwMakeContextCurrent(windowID);
        glfwSwapInterval(1);
        GL.createCapabilities();
        setupPerspective(Main.windowWidth, Main.windowHeight);
        glfwShowWindow(windowID);

        return windowID;
    }
    public void setupPerspective(int width, int height) {
        // 1. Tell OpenGL to use the full window area for rendering
        glViewport(0, 0, width, height);

        // 2. Switch to the Projection Matrix (used for setting up cameras/view scopes)
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        // 3. Define a flat 2D coordinate system matching the window pixels
        // Arguments: Left, Right, Bottom, Top, Near Z, Far Z
        glOrtho(0, width, height, 0, -1, 1);

        // 4. Switch back to the Modelview Matrix (used for moving/drawing objects)
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    public void render(Main main) {
        // 1. CLEAR: Wipe the screen to a solid color (e.g., dark blue)
        glClearColor(0.1f, 0.2f, 0.4f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glLoadIdentity();

        // Move to player position and apply your rotation
        glTranslatef(main.posx, main.posy, 0.0f);
        glRotatef(main.posr, 0.0f, 0.0f, 1.0f);

        glBegin(GL_QUADS);
        glColor3f(RED); // Red
        glVertex2f(-20.0f, -20.0f);  // Top Left

        glColor3f(GREEN); // Green
        glVertex2f(20.0f, -20.0f);   // Top Right

        glColor3f(BLUE); // Blue
        glVertex2f(20.0f, 20.0f);    // Bottom Right

        glColor3f(YELLOW); // Yellow
        glVertex2f(-20.0f, 20.0f);   // Bottom Left
        glEnd();

        glLoadIdentity();

        glPointSize(5.0f);
        glBegin(GL_POINTS);
            glColor3f(ORANGE);

            main.points.forEach(point -> glVertex2d(point.x, point.y));
        glEnd();

    }
    private void glColor3f(Color color) {
        GL11.glColor3f(color.r, color.g, color.b);
    }

    public static void window_close_callback(long windowID){
        window_close_callback(windowID, "unknown");
    }
    public static void window_close_callback(long windowID, String reason){
//        save();
        System.out.println("Requesting to close window: " + windowID + ", reason: " + reason);
    }

    private static class Color {
        public float r;
        public float g;
        public float b;

        public Color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }


    }
    private static class Point {
        public double x;
        public double y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}

