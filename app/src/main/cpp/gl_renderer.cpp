#include <jni.h>
#include <GLES3/gl3.h>
#include <string>

static GLuint program = 0;
static const char *vs = "#version 300 es\nlayout(location=0) in vec2 p; void main(){gl_Position=vec4(p,0.0,1.0);}";
static const char *fs = "#version 300 es\nprecision mediump float; out vec4 c; uniform float t; void main(){vec2 q=gl_FragCoord.xy/vec2(720.0,1280.0)-0.5; float r=length(q); float a=0.5+0.5*sin(t*2.0-r*24.0); vec3 col=mix(vec3(0.04,0.08,0.15),vec3(0.15,0.55,0.95),1.0-smoothstep(0.0,0.28,abs(r-0.18-a*0.012))); c=vec4(col,1.0);} ";
static GLuint shader(GLenum type, const char *src){ GLuint s=glCreateShader(type); glShaderSource(s,1,&src,nullptr); glCompileShader(s); return s; }
extern "C" JNIEXPORT void JNICALL Java_com_revival_assistant_NativeBridge_glInit(JNIEnv*, jclass){
 GLuint v=shader(GL_VERTEX_SHADER,vs), f=shader(GL_FRAGMENT_SHADER,fs); program=glCreateProgram(); glAttachShader(program,v); glAttachShader(program,f); glLinkProgram(program); glDeleteShader(v); glDeleteShader(f); glClearColor(0.03f,0.05f,0.09f,1.0f);
}
extern "C" JNIEXPORT void JNICALL Java_com_revival_assistant_NativeBridge_glResize(JNIEnv*, jclass, jint w, jint h){ glViewport(0,0,w,h); }
extern "C" JNIEXPORT void JNICALL Java_com_revival_assistant_NativeBridge_glDraw(JNIEnv*, jclass, jfloat time){
 glClear(GL_COLOR_BUFFER_BIT); if(!program)return; glUseProgram(program); GLint loc=glGetUniformLocation(program,"t"); glUniform1f(loc,time); const GLfloat verts[]={-1,-1, 1,-1, -1,1, 1,1}; GLuint buf; glGenBuffers(1,&buf); glBindBuffer(GL_ARRAY_BUFFER,buf); glBufferData(GL_ARRAY_BUFFER,sizeof(verts),verts,GL_STATIC_DRAW); glEnableVertexAttribArray(0); glVertexAttribPointer(0,2,GL_FLOAT,GL_FALSE,0,nullptr); glDrawArrays(GL_TRIANGLE_STRIP,0,4); glDeleteBuffers(1,&buf);
}
