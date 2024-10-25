MAC_APP_FRAMEWORKS=/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/System/Library/Frameworks
MAC_LIB_FRAMEWORKS=/System/Library/Frameworks

~/jextract-22/bin/jextract  \
    -t opengl \
    --output src/main/extracted-java/ \
    -l :${MAC_LIB_FRAMEWORKS}/GLUT.framework/GLUT \
    -l :${MAC_LIB_FRAMEWORKS}/OpenGL.framework/OpenGL \
    ${MAC_APP_FRAMEWORKS}/GLUT.framework/Headers/glut.h


~/jextract-22/bin/jextract \
   -t opencl \
   --output src/main/extracted-java/ \
   -l :${MAC_LIB_FRAMEWORKS}/OpenCL.framework/OpenCL \
   ${MAC_APP_FRAMEWORKS}/OpenCL.framework/Headers/opencl.h

rm -rf classes
mkdir -p classes

javac \
    --source 24 \
    --enable-preview \
    -d classes \
    --source-path src/main/extracted-java:src/main/java \
    $(find src/main/java -name "*.java") \
    $(find src/main/extracted-java -name "*.java") \
