/*
 * Copyright (c) 2002-2010 LWJGL Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package cz.mtheory.example.opencl;

import static java.lang.Math.min;
import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_CPU;
import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_GPU;
import static org.lwjgl.opencl.CL10.CL_MAP_WRITE;
import static org.lwjgl.opencl.CL10.CL_MEM_READ_ONLY;
import static org.lwjgl.opencl.CL10.CL_MEM_WRITE_ONLY;
import static org.lwjgl.opencl.CL10.CL_PROGRAM_BUILD_LOG;
import static org.lwjgl.opencl.CL10.CL_QUEUE_PROFILING_ENABLE;
import static org.lwjgl.opencl.CL10.CL_TRUE;
import static org.lwjgl.opencl.CL10.clBuildProgram;
import static org.lwjgl.opencl.CL10.clCreateBuffer;
import static org.lwjgl.opencl.CL10.clCreateCommandQueue;
import static org.lwjgl.opencl.CL10.clCreateKernel;
import static org.lwjgl.opencl.CL10.clCreateProgramWithSource;
import static org.lwjgl.opencl.CL10.clEnqueueMapBuffer;
import static org.lwjgl.opencl.CL10.clEnqueueNDRangeKernel;
import static org.lwjgl.opencl.CL10.clEnqueueUnmapMemObject;
import static org.lwjgl.opencl.CL10.clEnqueueWaitForEvents;
import static org.lwjgl.opencl.CL10.clFinish;
import static org.lwjgl.opencl.CL10.clReleaseContext;
import static org.lwjgl.opencl.CL10.clReleaseMemObject;
import static org.lwjgl.opencl.CL10.clReleaseProgram;
import static org.lwjgl.opencl.CL10GL.clCreateFromGLBuffer;
import static org.lwjgl.opencl.CL10GL.clCreateFromGLTexture2D;
import static org.lwjgl.opencl.CL10GL.clEnqueueAcquireGLObjects;
import static org.lwjgl.opencl.CL10GL.clEnqueueReleaseGLObjects;
import static org.lwjgl.opencl.KHRGLEvent.clCreateEventFromGLsyncKHR;
import static org.lwjgl.opengl.AMDDebugOutput.glDebugMessageCallbackAMD;
import static org.lwjgl.opengl.ARBCLEvent.glCreateSyncFromCLeventARB;
import static org.lwjgl.opengl.ARBDebugOutput.glDebugMessageCallbackARB;
import static org.lwjgl.opengl.ARBSync.GL_SYNC_GPU_COMMANDS_COMPLETE;
import static org.lwjgl.opengl.ARBSync.glFenceSync;
import static org.lwjgl.opengl.ARBSync.glWaitSync;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_COMPILE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDeleteLists;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawPixels;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glRasterPos2i;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glVertex2i;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLDeviceCapabilities;
import org.lwjgl.opencl.CLEvent;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opencl.api.Filter;
import org.lwjgl.opengl.AMDDebugOutputCallback;
import org.lwjgl.opengl.ARBDebugOutputCallback;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.GLSync;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.ReadableColor;

/*
		THIS DEMO USES CODE PORTED FROM JogAmp.org
		Original code: http://github.com/mbien/jocl-demos
		Original author: Michael Bien

   ___         ___                      ___
	  /  /\       /  /\         ___        /  /\    http://jocl.jogamp.org/
	 /  /:/      /  /::\       /__/\      /  /::\   a http://jogamp.org/ project.
	/__/::\     /  /:/\:\      \  \:\    /  /:/\:\
	\__\/\:\   /  /:/~/::\      \  \:\  /  /:/~/::\
	   \  \:\ /__/:/ /:/\:\ ___  \__\:\/__/:/ /:/\:\
		\__\:\\  \:\/:/__\//__/\ |  |:|\  \:\/:/__\/
		/  /:/ \  \::/     \  \:\|  |:| \  \::/
	   /__/:/   \  \:\      \  \:\__|:|  \  \:\
	   \__\/     \  \:\      \__\::::/    \  \:\
				  \__\/          ~~~~      \__\/
			   ___          ___       ___          ___          ___
			  /  /\        /  /\     /  /\        /__/\        /  /\
			 /  /::\      /  /::\   /  /:/_       \  \:\      /  /:/
			/  /:/\:\    /  /:/\:\ /  /:/ /\       \  \:\    /  /:/      ___     ___
		   /  /:/  \:\  /  /:/~/://  /:/ /:/_  _____\__\:\  /  /:/  ___ /__/\   /  /\
		  /__/:/ \__\:\/__/:/ /://__/:/ /:/ /\/__/::::::::\/__/:/  /  /\\  \:\ /  /:/
		  \  \:\ /  /:/\  \:\/:/ \  \:\/:/ /:/\  \:\~~\~~\/\  \:\ /  /:/ \  \:\  /:/
		   \  \:\  /:/  \  \::/   \  \::/ /:/  \  \:\  ~~~  \  \:\  /:/   \  \:\/:/
			\  \:\/:/    \  \:\    \  \:\/:/    \  \:\       \  \:\/:/     \  \::/
			 \  \::/      \  \:\    \  \::/      \  \:\       \  \::/       \__\/
			  \__\/        \__\/     \__\/        \__\/        \__\/

		 _____          ___           ___           ___           ___
		/  /::\        /  /\         /__/\         /  /\         /  /\
	   /  /:/\:\      /  /:/_       |  |::\       /  /::\       /  /:/_
	  /  /:/  \:\    /  /:/ /\      |  |:|:\     /  /:/\:\     /  /:/ /\
	 /__/:/ \__\:|  /  /:/ /:/_   __|__|:|\:\   /  /:/  \:\   /  /:/ /::\
	 \  \:\ /  /:/ /__/:/ /:/ /\ /__/::::| \:\ /__/:/ \__\:\ /__/:/ /:/\:\
	  \  \:\  /:/  \  \:\/:/ /:/ \  \:\~~\__\/ \  \:\ /  /:/ \  \:\/:/~/:/
	   \  \:\/:/    \  \::/ /:/   \  \:\        \  \:\  /:/   \  \::/ /:/
		\  \::/      \  \:\/:/     \  \:\        \  \:\/:/     \__\/ /:/
		 \__\/        \  \::/       \  \:\        \  \::/        /__/:/
					   \__\/         \__\/         \__\/         \__\/
*/

/**
 * Computes the Mandelbrot set with OpenCL using multiple GPUs and renders the
 * result with OpenGL. A shared PBO is used as storage for the fractal image.<br/>
 * http://en.wikipedia.org/wiki/Mandelbrot_set
 * <p>
 * controls:<br/>
 * keys 1-9 control parallelism level<br/>
 * space enables/disables slice seperator<br/>
 * 'd' toggles between 32/64bit floatingpoint precision<br/>
 * mouse/mousewheel to drag and zoom<br/>
 * 'Home' to reset the viewport<br/>
 * </p>
 * 
 * @author Michael Bien, Spasi
 */
public class DemoFractal {

    // max number of used GPUs
    private static final int MAX_PARALLELISM_LEVEL = 8;

    private static final int COLOR_MAP_SIZE = 32 * 2 * 4;

    private Set<String> params;

    private CLContext clContext;
    private CLCommandQueue[] queues;
    private CLKernel[] kernels;
    private CLProgram[] programs;

    private CLMem[] glBuffers;
    private IntBuffer glIDs;

    private boolean useTextures;

    // Texture rendering
    private int dlist;
    private int vsh;
    private int fsh;
    private int program;

    private CLMem[] colorMap;

    private final PointerBuffer kernel2DGlobalWorkSize;

    // max per pixel iterations to compute the fractal
    private int maxIterations = 500;

    private int width = 512;
    private int height = 512;

    private double minX = -2f;
    private double minY = -1.2f;
    private double maxX = 0.6f;
    private double maxY = 1.3f;

    private boolean dragging;
    private double dragX;
    private double dragY;
    private double dragMinX;
    private double dragMinY;
    private double dragMaxX;
    private double dragMaxY;

    private int mouseX;
    private int mouseY;

    private int slices;

    private boolean drawSeparator;
    private boolean doublePrecision = true;
    private boolean buffersInitialized;
    private boolean rebuild;

    private boolean run = true;

    // EVENT SYNCING

    private final PointerBuffer syncBuffer = BufferUtils.createPointerBuffer(1);

    private boolean syncGLtoCL; // true if we can make GL wait on events generated from CL queues.
    private CLEvent[] clEvents;
    private GLSync[] clSyncs;

    private boolean syncCLtoGL; // true if we can make CL wait on sync objects generated from GL.
    private GLSync glSync;
    private CLEvent glEvent;

    public DemoFractal(final String[] args) {
        params = new HashSet<String>();

        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];

            if (arg.charAt(0) != '-' && arg.charAt(0) != '/') throw new IllegalArgumentException("Invalid command-line argument: " +
                    args[i]);

            final String param = arg.substring(1);

            if ("forcePBO".equalsIgnoreCase(param))
                params.add("forcePBO");
            else if ("forceCPU".equalsIgnoreCase(param))
                params.add("forceCPU");
            else if ("debugGL".equalsIgnoreCase(param))
                params.add("debugGL");
            else if ("iterations".equalsIgnoreCase(param)) {
                if (args.length < i + 1 + 1) throw new IllegalArgumentException("Invalid iterations argument specified.");

                try {
                    this.maxIterations = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number of iterations specified.");
                }
            } else if ("res".equalsIgnoreCase(param)) {
                if (args.length < i + 2 + 1) throw new IllegalArgumentException("Invalid res argument specified.");

                try {
                    this.width = Integer.parseInt(args[++i]);
                    this.height = Integer.parseInt(args[++i]);

                    if (width < 1 || height < 1) throw new IllegalArgumentException("Invalid res dimensions specified.");
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid res dimensions specified.");
                }
            }
        }

        kernel2DGlobalWorkSize = BufferUtils.createPointerBuffer(2);
    }

    public static void main(String args[]) {
        DemoFractal demo = new DemoFractal(args);
        demo.init();
        demo.run();
    }

    public void init() {
        try {
            CL.create();
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.setTitle("OpenCL Fractal Demo");
            Display.setSwapInterval(0);
            Display.create(new PixelFormat(), new ContextAttribs().withDebug(params.contains("debugGL")));
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }

        try {
            initCL(Display.getDrawable());
        } catch (Exception e) {
            if (clContext != null) clReleaseContext(clContext);
            Display.destroy();
            throw new RuntimeException(e);
        }

        glDisable(GL_DEPTH_TEST);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        initView(Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());

        initGLObjects();
        glFinish();

        setKernelConstants();
    }

    private void initCL(Drawable drawable) throws Exception {
        // Find a platform
        List<CLPlatform> platforms = CLPlatform.getPlatforms();
        if (platforms == null) throw new RuntimeException("No OpenCL platforms found.");

        final CLPlatform platform = platforms.get(params.contains("forceCPU") ? 1 : 0); // just grab the first one

        // Find devices with GL sharing support
        final Filter<CLDevice> glSharingFilter = new Filter<CLDevice>() {
            @Override
            public boolean accept(final CLDevice device) {
                final CLDeviceCapabilities caps = CLCapabilities.getDeviceCapabilities(device);
                return caps.CL_KHR_gl_sharing;
            }
        };
        int device_type = params.contains("forceCPU") ? CL_DEVICE_TYPE_CPU : CL_DEVICE_TYPE_GPU;
        List<CLDevice> devices = platform.getDevices(device_type, glSharingFilter);
        if (devices == null) {
            device_type = CL_DEVICE_TYPE_CPU;
            devices = platform.getDevices(device_type, glSharingFilter);
            if (devices == null) throw new RuntimeException("No OpenCL devices found with KHR_gl_sharing support.");
        }

        // Create the context
        clContext = CLContext.create(platform, devices, new CLContextCallback() {
            @Override
            protected void handleMessage(final String errinfo, final ByteBuffer private_info) {
                System.out.println("[CONTEXT MESSAGE] " + errinfo);
            }
        }, drawable, null);

        slices = min(devices.size(), MAX_PARALLELISM_LEVEL);

        // create command queues for every GPU, setup colormap and init kernels
        queues = new CLCommandQueue[slices];
        kernels = new CLKernel[slices];
        colorMap = new CLMem[slices];

        for (int i = 0; i < slices; i++) {
            colorMap[i] = clCreateBuffer(clContext, CL_MEM_READ_ONLY, COLOR_MAP_SIZE, null);
            colorMap[i].checkValid();

            // create command queue and upload color map buffer on each used device
            queues[i] = clCreateCommandQueue(clContext, devices.get(i), CL_QUEUE_PROFILING_ENABLE, null);
            queues[i].checkValid();

            final ByteBuffer colorMapBuffer = clEnqueueMapBuffer(queues[i], colorMap[i], CL_TRUE, CL_MAP_WRITE, 0, COLOR_MAP_SIZE, null, null, null);
            initColorMap(colorMapBuffer.asIntBuffer(), 32, ReadableColor.BLUE, ReadableColor.GREEN, ReadableColor.RED);
            clEnqueueUnmapMemObject(queues[i], colorMap[i], colorMapBuffer, null, null);
        }

        // check if we have 64bit FP support on all devices
        // if yes we can use only one program for all devices + one kernel per device.
        // if not we will have to create (at least) one program for 32 and one for 64bit devices.
        // since there are different vendor extensions for double FP we use one program per device.
        // (OpenCL spec is not very clear about this usecases)
        boolean all64bit = true;
        for (CLDevice device : devices) {
            if (!isDoubleFPAvailable(device)) {
                all64bit = false;
                break;
            }
        }

        // load program(s)
        programs = new CLProgram[all64bit ? 1 : slices];

        final ContextCapabilities caps = GLContext.getCapabilities();

        if (!caps.OpenGL20)
            throw new RuntimeException("OpenGL 2.0 is required to run this demo.");
        else if (device_type == CL_DEVICE_TYPE_CPU && !caps.OpenGL21) throw new RuntimeException("OpenGL 2.1 is required to run this demo.");

        if (params.contains("debugGL")) {
            if (caps.GL_ARB_debug_output)
                glDebugMessageCallbackARB(new ARBDebugOutputCallback());
            else if (caps.GL_AMD_debug_output) glDebugMessageCallbackAMD(new AMDDebugOutputCallback());
        }

        if (device_type == CL_DEVICE_TYPE_GPU)
            System.out.println("OpenCL Device Type: GPU (Use -forceCPU to use CPU)");
        else
            System.out.println("OpenCL Device Type: CPU");
        for (int i = 0; i < devices.size(); i++)
            System.out.println("OpenCL Device #" + (i + 1) + " supports KHR_gl_event = " +
                    CLCapabilities.getDeviceCapabilities(devices.get(i)).CL_KHR_gl_event);

        System.out.println("\nMax Iterations: " + maxIterations + " (Use -iterations <count> to change)");
        System.out.println("Display resolution: " + width + "x" + height + " (Use -res <width> <height> to change)");

        System.out.println("\nOpenGL caps.GL_ARB_sync = " + caps.GL_ARB_sync);
        System.out.println("OpenGL caps.GL_ARB_cl_event = " + caps.GL_ARB_cl_event);

        // Use PBO if we're on a CPU implementation
        useTextures = device_type == CL_DEVICE_TYPE_GPU && (!caps.OpenGL21 || !params.contains("forcePBO"));
        if (useTextures) {
            System.out.println("\nCL/GL Sharing method: TEXTURES (use -forcePBO to use PBO + DrawPixels)");
            System.out.println("Rendering method: Shader on a fullscreen quad");
        } else {
            System.out.println("\nCL/GL Sharing method: PIXEL BUFFER OBJECTS");
            System.out.println("Rendering method: DrawPixels");
        }

        buildPrograms();

        // Detect GLtoCL synchronization method
        syncGLtoCL = caps.GL_ARB_cl_event; // GL3.2 or ARB_sync implied
        if (syncGLtoCL) {
            clEvents = new CLEvent[slices];
            clSyncs = new GLSync[slices];
            System.out.println("\nGL to CL sync: Using OpenCL events");
        } else
            System.out.println("\nGL to CL sync: Using clFinish");

        // Detect CLtoGL synchronization method
        syncCLtoGL = caps.OpenGL32 || caps.GL_ARB_sync;
        if (syncCLtoGL) {
            for (CLDevice device : devices) {
                if (!CLCapabilities.getDeviceCapabilities(device).CL_KHR_gl_event) {
                    syncCLtoGL = false;
                    break;
                }
            }
        }
        if (syncCLtoGL) {
            System.out.println("CL to GL sync: Using OpenGL sync objects");
        } else
            System.out.println("CL to GL sync: Using glFinish");

        if (useTextures) {
            dlist = glGenLists(1);

            glNewList(dlist, GL_COMPILE);
            glBegin(GL_QUADS);
            {
                glTexCoord2f(0.0f, 0.0f);
                glVertex2f(0, 0);

                glTexCoord2f(0.0f, 1.0f);
                glVertex2i(0, height);

                glTexCoord2f(1.0f, 1.0f);
                glVertex2f(width, height);

                glTexCoord2f(1.0f, 0.0f);
                glVertex2f(width, 0);
            }
            glEnd();
            glEndList();

            vsh = glCreateShader(GL_VERTEX_SHADER);
            glShaderSource(vsh, "varying vec2 texCoord;\n" + "\n" + "void main(void) {\n"
                    + "\tgl_Position = ftransform();\n" + "\ttexCoord = gl_MultiTexCoord0.xy;\n" + "}");
            glCompileShader(vsh);

            fsh = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fsh, "uniform sampler2D mandelbrot;\n" + "\n" + "varying vec2 texCoord;\n" + "\n"
                    + "void main(void) {\n" + "\tgl_FragColor = texture2D(mandelbrot, texCoord);" + "}");
            glCompileShader(fsh);

            program = glCreateProgram();
            glAttachShader(program, vsh);
            glAttachShader(program, fsh);
            glLinkProgram(program);

            glUseProgram(program);
            glUniform1i(glGetUniformLocation(program, "mandelbrot"), 0);
        }

        System.out.println("");
    }

    private void buildPrograms() {
        /*
         * workaround: The driver keeps using the old binaries for some reason.
         * to solve this we simple create a new program and release the old.
         * however rebuilding programs should be possible -> remove when drivers are fixed.
         * (again: the spec is not very clear about this kind of usages)
         */
        if (programs[0] != null) {
            for (CLProgram program : programs)
                clReleaseProgram(program);
        }

        try {
            createPrograms();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // disable 64bit floating point math if not available
        for (int i = 0; i < programs.length; i++) {
            final CLDevice device = queues[i].getCLDevice();

            final StringBuilder options = new StringBuilder(useTextures ? "-D USE_TEXTURE" : "");
            final CLDeviceCapabilities caps = CLCapabilities.getDeviceCapabilities(device);
            if (doublePrecision && isDoubleFPAvailable(device)) {
                //cl_khr_fp64
                options.append(" -D DOUBLE_FP");

                //amd's verson of double precision floating point math
                if (!caps.CL_KHR_fp64 && caps.CL_AMD_fp64) options.append(" -D AMD_FP");
            }

            System.out.println("\nOpenCL COMPILER OPTIONS: " + options);

            try {
                clBuildProgram(programs[i], device, options, null);
            } finally {
                System.out.println("BUILD LOG: " + programs[i].getBuildInfoString(device, CL_PROGRAM_BUILD_LOG));
            }
        }

        rebuild = false;

        // init kernel with constants
        for (int i = 0; i < kernels.length; i++)
            kernels[i] = clCreateKernel(programs[min(i, programs.length)], "mandelbrot", null);
    }

    private void initGLObjects() {
        if (glBuffers == null) {
            glBuffers = new CLMem[slices];
            glIDs = BufferUtils.createIntBuffer(slices);
        } else {
            for (CLMem mem : glBuffers)
                clReleaseMemObject(mem);

            if (useTextures)
                glDeleteTextures(glIDs);
            else
                glDeleteBuffers(glIDs);
        }

        if (useTextures)
            glGenTextures(glIDs);
        else
            glGenBuffers(glIDs);

        if (useTextures) {
            // Init textures
            for (int i = 0; i < slices; i++) {
                glBindTexture(GL_TEXTURE_2D, glIDs.get(i));
                glTexImage2D(GL_TEXTURE_2D, 0, GL11.GL_RGBA, width / slices, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

                glBuffers[i] = clCreateFromGLTexture2D(clContext, CL_MEM_WRITE_ONLY, GL_TEXTURE_2D, 0, glIDs.get(i), null);
            }
            glBindTexture(GL_TEXTURE_2D, 0);
        } else {
            // setup one empty PBO per slice
            for (int i = 0; i < slices; i++) {
                glBindBuffer(GL_PIXEL_UNPACK_BUFFER, glIDs.get(i));
                glBufferData(GL_PIXEL_UNPACK_BUFFER, width * height * 4 / slices, GL_STREAM_DRAW);

                glBuffers[i] = clCreateFromGLBuffer(clContext, CL_MEM_WRITE_ONLY, glIDs.get(i), null);
            }
            glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
        }

        buffersInitialized = true;
    }

    // init kernels with constants

    private void setKernelConstants() {
        for (int i = 0; i < slices; i++) {
            kernels[i].setArg(6, glBuffers[i]).setArg(7, colorMap[i]).setArg(8, COLOR_MAP_SIZE).setArg(9, maxIterations);
        }
    }

    // rendering cycle

    private void run() {
        long startTime = System.currentTimeMillis() + 5000;
        long fps = 0;

        while (run) {
            if (!Display.isVisible()) Thread.yield();

            handleIO();
            display();

            Display.update();
            if (Display.isCloseRequested()) break;

            if (startTime > System.currentTimeMillis()) {
                fps++;
            } else {
                long timeUsed = 5000 + (startTime - System.currentTimeMillis());
                startTime = System.currentTimeMillis() + 5000;
                System.out.println(fps + " frames in 5 seconds = " + (fps / (timeUsed / 1000f)));
                fps = 0;
            }
        }

        clReleaseContext(clContext);

        if (useTextures) {
            glDeleteProgram(program);
            glDeleteShader(fsh);
            glDeleteShader(vsh);

            glDeleteLists(dlist, 1);
        }

        CL.destroy();
        Display.destroy();
    }

    public void display() {
        // TODO: Need to clean-up events, test when ARB_cl_events & KHR_gl_event are implemented.

        // make sure GL does not use our objects before we start computing
        if (syncCLtoGL && glEvent != null) {
            for (final CLCommandQueue queue : queues)
                clEnqueueWaitForEvents(queue, glEvent);
        } else
            glFinish();

        if (!buffersInitialized) {
            initGLObjects();
            setKernelConstants();
        }

        if (rebuild) {
            buildPrograms();
            setKernelConstants();
        }
        compute(doublePrecision);

        render();
    }

    // OpenCL

    private void compute(final boolean is64bit) {
        int sliceWidth = (int) (width / (float) slices);
        double rangeX = (maxX - minX) / slices;
        double rangeY = (maxY - minY);

        kernel2DGlobalWorkSize.put(0, sliceWidth).put(1, height);

        // start computation
        for (int i = 0; i < slices; i++) {
            kernels[i].setArg(0, sliceWidth).setArg(1, height);
            if (!is64bit || !isDoubleFPAvailable(queues[i].getCLDevice())) {
                kernels[i].setArg(2, (float) (minX + rangeX * i)).setArg(3, (float) minY).setArg(4, (float) rangeX).setArg(5, (float) rangeY);
            } else {
                kernels[i].setArg(2, minX + rangeX * i).setArg(3, minY).setArg(4, rangeX).setArg(5, rangeY);
            }

            // acquire GL objects, and enqueue a kernel with a probe from the list
            clEnqueueAcquireGLObjects(queues[i], glBuffers[i], null, null);

            clEnqueueNDRangeKernel(queues[i], kernels[i], 2, null, kernel2DGlobalWorkSize, null, null, null);

            clEnqueueReleaseGLObjects(queues[i], glBuffers[i], null, syncGLtoCL ? syncBuffer : null);
            if (syncGLtoCL) {
                clEvents[i] = queues[i].getCLEvent(syncBuffer.get(0));
                clSyncs[i] = glCreateSyncFromCLeventARB(queues[i].getParent(), clEvents[i], 0);
            }
        }

        // block until done (important: finish before doing further gl work)
        if (!syncGLtoCL) {
            for (int i = 0; i < slices; i++)
                clFinish(queues[i]);
        }
    }

    // OpenGL

    @SuppressWarnings("unused")
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT);

        if (syncGLtoCL) {
            for (int i = 0; i < slices; i++)
                glWaitSync(clSyncs[i], 0, 0);
        }

        //draw slices
        int sliceWidth = width / slices;

        if (useTextures) {
            for (int i = 0; i < slices; i++) {
                int seperatorOffset = drawSeparator ? i : 0;

                glBindTexture(GL_TEXTURE_2D, glIDs.get(i));
                glCallList(dlist);
            }
        } else {
            for (int i = 0; i < slices; i++) {
                int seperatorOffset = drawSeparator ? i : 0;

                glBindBuffer(GL_PIXEL_UNPACK_BUFFER, glIDs.get(i));
                glRasterPos2i(sliceWidth * i + seperatorOffset, 0);

                glDrawPixels(sliceWidth, height, GL_RGBA, GL_UNSIGNED_BYTE, 0);
            }
            glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
        }

        if (syncCLtoGL) {
            glSync = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
            glEvent = clCreateEventFromGLsyncKHR(clContext, glSync, null);
        }

        //draw info text
        /*
        textRenderer.beginRendering(width, height, false);

        textRenderer.draw("device/time/precision", 10, height - 15);

        for ( int i = 0; i < slices; i++ ) {
        	CLDevice device = queues[i].getDevice();
        	boolean doubleFP = doublePrecision && isDoubleFPAvailable(device);
        	CLEvent event = probes.getEvent(i);
        	long start = event.getProfilingInfo(START);
        	long end = event.getProfilingInfo(END);
        	textRenderer.draw(device.getType().toString() + i + " "
        	                  + (int)((end - start) / 1000000.0f) + "ms @"
        	                  + (doubleFP ? "64bit" : "32bit"), 10, height - (20 + 16 * (slices - i)));
        }

        textRenderer.endRendering();
        */
    }

    private void handleIO() {
        if (Keyboard.getNumKeyboardEvents() != 0) {
            while (Keyboard.next()) {
                if (Keyboard.getEventKeyState()) continue;

                final int key = Keyboard.getEventKey();

                if (Keyboard.KEY_1 <= key && key <= Keyboard.KEY_8) {
                    int number = key - Keyboard.KEY_1 + 1;
                    slices = min(number, min(queues.length, MAX_PARALLELISM_LEVEL));
                    System.out.println("NEW PARALLELISM LEVEL: " + slices);
                    buffersInitialized = false;
                } else {
                    switch (Keyboard.getEventKey()) {
                        case Keyboard.KEY_SPACE:
                            drawSeparator = !drawSeparator;
                            System.out.println("SEPARATOR DRAWING IS NOW: " + (drawSeparator ? "ON" : "OFF"));
                            break;
                        case Keyboard.KEY_D:
                            doublePrecision = !doublePrecision;
                            System.out.println("DOUBLE PRECISION IS NOW: " + (doublePrecision ? "ON" : "OFF"));
                            rebuild = true;
                            break;
                        case Keyboard.KEY_HOME:
                            minX = -2f;
                            minY = -1.2f;
                            maxX = 0.6f;
                            maxY = 1.3f;
                            break;
                        case Keyboard.KEY_ESCAPE:
                            run = false;
                            break;
                    }
                }
            }
        }

        while (Mouse.next()) {
            final int eventBtn = Mouse.getEventButton();

            final int x = Mouse.getX();
            final int y = Mouse.getY();

            if (Mouse.isButtonDown(0) && (x != mouseX || y != mouseY)) {
                if (!dragging) {
                    dragging = true;

                    dragX = mouseX;
                    dragY = mouseY;

                    dragMinX = minX;
                    dragMinY = minY;
                    dragMaxX = maxX;
                    dragMaxY = maxY;
                }

                double offsetX = (x - dragX) * (maxX - minX) / width;
                double offsetY = (y - dragY) * (maxY - minY) / height;

                minX = dragMinX - offsetX;
                minY = dragMinY - offsetY;

                maxX = dragMaxX - offsetX;
                maxY = dragMaxY - offsetY;
            } else {
                if (dragging) dragging = false;

                if (eventBtn == -1) {
                    final int dwheel = Mouse.getEventDWheel();
                    if (dwheel != 0) {
                        double scaleFactor = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ||
                                Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) ? 0.25 : 0.05;
                        double scale = dwheel > 0 ? scaleFactor : -scaleFactor;

                        double deltaX = scale * (maxX - minX);
                        double deltaY = scale * (maxY - minY);

                        // offset for "zoom to cursor"
                        double offsetX = (x / (double) width - 0.5) * deltaX * 2.0;
                        double offsetY = (y / (double) height - 0.5) * deltaY * 2.0;

                        minX += deltaX + offsetX;
                        minY += deltaY - offsetY;

                        maxX += -deltaX + offsetX;
                        maxY += -deltaY - offsetY;
                    }
                }
            }

            mouseX = x;
            mouseY = y;
        }
    }

    private static boolean isDoubleFPAvailable(CLDevice device) {
        final CLDeviceCapabilities caps = CLCapabilities.getDeviceCapabilities(device);
        return caps.CL_KHR_fp64 || caps.CL_AMD_fp64;
    }

    private void createPrograms() throws IOException {
        final String source = getProgramSource("cz/mtheory/example/opencl/Mandelbrot.cl");
        for (int i = 0; i < programs.length; i++)
            programs[i] = clCreateProgramWithSource(clContext, source, null);
    }

    private String getProgramSource(final String file) throws IOException {
        InputStream source = null;
        URL sourceURL = Thread.currentThread().getContextClassLoader().getResource(file);
        if (sourceURL != null) {
            source = sourceURL.openStream();
        }
        if (source == null) // dev-mode
        source = new FileInputStream("src/java/" + file);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(source));

        final StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
        } finally {
            source.close();
        }

        return sb.toString();
    }

    private static void initColorMap(IntBuffer colorMap, int stepSize, ReadableColor... colors) {
        for (int n = 0; n < colors.length - 1; n++) {
            ReadableColor color = colors[n];
            int r0 = color.getRed();
            int g0 = color.getGreen();
            int b0 = color.getBlue();

            color = colors[n + 1];
            int r1 = color.getRed();
            int g1 = color.getGreen();
            int b1 = color.getBlue();

            int deltaR = r1 - r0;
            int deltaG = g1 - g0;
            int deltaB = b1 - b0;

            for (int step = 0; step < stepSize; step++) {
                float alpha = (float) step / (stepSize - 1);
                int r = (int) (r0 + alpha * deltaR);
                int g = (int) (g0 + alpha * deltaG);
                int b = (int) (b0 + alpha * deltaB);
                colorMap.put((r << 0) | (g << 8) | (b << 16));
            }
        }
    }

    private static void initView(int width, int height) {
        glViewport(0, 0, width, height);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, width, 0.0, height, 0.0, 1.0);
    }

}