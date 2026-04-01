> [!IMPORTANT]
> This is a fork of the [luaj/luaj](https://github.com/luaj/luaj) GitHub repository, with some adjustments and changes
> to my needs.

> [!NOTE]
> There are currently no examples provided - thus all links to examples do not work at the moment.

# Getting Started with LuaJ

<small>
Copyright &copy; 2009-2014 Luaj.org.
Freely available under the terms of the
[LuaJ License](LICENSE).
</small>

---

[introduction](#1---introduction)
&middot;
[examples](#2---examples)
&middot;
[concepts](#3---concepts)
&middot;
[libraries](#4---libraries)
&middot;
[luaj api](#5---luaj-api)
&middot;
[parser](#6---parser)
&middot;
[building](#7---building-and-testing)
&middot;
[downloads](#8---downloads)
&middot;
[release notes](#9---release-notes)

---


# 1 - Introduction
## Goals of Luaj
Luaj is a lua interpreter based on the 5.2.x version of lua with the following goals in mind:*   Java-centric implementation of lua vm built to leverage standard Java features.
*   Lightweight, high performance execution of lua.
*   Multi-platform to be able to run on JME, JSE, or JEE environments.
*   Complete set of libraries and tools for integration into real-world projects.
*   Dependable due to sufficient unit testing of vm and library features.

## Luaj version and Lua Versions
### Luaj 3.0.x
Support for lua 5.2.x features:
*   _ENV environments model.
*   yield from pcall or metatags.
*   Bitwise operator library.

It also includes miscellaneous improvements over luaj 2.0.x:
*   Better thread safety.
*   More compatible table behavior.
*   Better coroutine-related garbage collection.
*   Maven integration.
*   Better debug reporting when using closures.
*   Line numbers in parse syntax tree.

### Luaj 2.0.x
Support for lua 5.1.x features, plus:
*   Support for compiling lua source code into Java source code.
*   Support for compiling lua bytecode directly into Java bytecode.
*   Stackless vm design centered around dynamically typed objects.
*   Good alignment with C API (see [names.csv](names.csv) for details)
*   Implementation of weak keys and values, and all metatags.

### Luaj 1.0.x
Support for most lua 5.1.x features.

## Performance
Good performance is a major goal of luaj.  
The following table provides measured execution times on a subset of benchmarks from
[the computer language benchmarks game](http://shootout.alioth.debian.org/)
in comparison with the standard C distribution.
|          |         |                   |                    |                                |               |              |      |                                                                         |          |                |
|----------|---------|-------------------|--------------------|--------------------------------|---------------|--------------|------|-------------------------------------------------------------------------|----------|----------------|
| Project  | Version | Mode              |                    | Benchmark execution time (sec) |               |              |      |                                                                         | Language | Sample command |
|          |         |                   | *binarytrees 15* | *fannkuch 10*                | *nbody 1e6* | *nsieve 9* |      |                                                                         |          |                |
| luaj     | 3.0     | -b (luajc)       | 2.980              | 5.073                          | 16.794        | 11.274       | Java | java -cp luaj-jse-3.0.2.jar;bcel-5.2.jar lua **-b** fannkuch.lua 10 |          |                |
|          |         | -n (interpreted) | 12.838             | 23.290                         | 36.894        | 15.163       |      | java -cp luaj-jse-3.0.2.jar lua -n fannkuch.lua 10                      |          |                |
| lua      | 5.1.4   |                   | 17.637             | 16.044                         | 15.201        | 5.477        | C    | lua fannkuch.lua 10                                                     |          |                |
| jill     | 1.0.1   |                   | 44.512             | 54.630                         | 72.172        | 20.779       | Java |                                                                         |          |                |
| kahlua   | 1.0     | jse               | 22.963             | 63.277                         | 68.223        | 21.529       | Java |                                                                         |          |                |
| mochalua | 1.0     |                   | 50.457             | 70.368                         | 82.868        | 41.262       | Java |                                                                         |          |                |

Luaj in interpreted mode performs well for the benchmarks, and even better when
the lua-to-java-bytecode (luajc) compiler is used,
and actually executes *faster* than C-based lua in some cases.  
It is also faster than Java-lua implementations Jill, Kahlua, and Mochalua for all benchmarks tested.

# 2 - Examples
## Run a Lua script in Java SE

From the main distribution directory line type:
```shell
java -cp luaj-jse-[version].jar lua examples/lua/hello.lua
```

You should see the following output:
```text
hello, world
```

To see how luaj can be used to acccess most Java API's including swing, try:
```shell
java -cp luaj-jse-[version].jar lua examples/lua/swingapp.lua
```

Links to sources:

[examples/lua/hello.lua](examples/lua/hello.lua);
[examples/lua/swingapp.lua](examples/lua/swingapp.lua)

# Compile Lua source to lua bytecode

From the main distribution directory line type:
```shell
java -cp luaj-jse-[version].jar luac examples/lua/hello.lua
java -cp luaj-jse-[version].jar lua luac.out
```

The compiled output "luac.out" is lua bytecode and should run and produce the same result.

## Compile Lua source or bytecode to java bytecode

Luaj can compile Lua sources or binaries directly to java bytecode if the bcel library is on the class path. From the main distribution directory line type:

```shell
ant bcel-lib
java -cp &quot;luaj-jse-[version].jar;lib/bcel-5.2.jar&quot; luajc -s examples/lua -d . hello.lua
java -cp &quot;luaj-jse-[version].jar;.&quot; lua -l hello
```

The output `hello.class` is Java bytecode, should run and produce the same result.
There is no runtime dependency on the bcel library, 
but the compiled classes must be in the class path at runtime, unless runtime jit-compiling via luajc and bcel are desired (see later sections).

Lua scripts can also be run directly in this mode without precompiling using the `lua` command with the **`-b`** option and providing the `bcel` library in the class path:
```shell
java -cp &quot;luaj-jse-[version].jar;lib/bcel-5.2.jar&quot; lua -b examples/lua/hello.lua
```

## Run a script in a Java Application

A simple hello, world example in luaj is:
```java
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

Globals globals = JsePlatform.standardGlobals();
LuaValue chunk = globals.load("print 'hello, world'");
chunk.call();
```

Loading from a file is done via Globals.loadFile():
```java
LuaValue chunk = globals.loadfile("examples/lua/hello.lua");
```

Chunks can also be loaded from a `Reader` as text source
```java
chunk = globals.load(new StringReader("print 'hello, world'"), "main.lua");
```

or an InputStream to be loaded as text source "t", or binary lua file "b":
```java
chunk = globals.load(new FileInputSStream("examples/lua/hello.lua"), "main.lua", "bt"));
```

A simple example may be found in
[examples/jse/SampleJseMain.java](examples/jse/SampleJseMain.java)

You must include the library **luaj-jse-3.0.2.jar** in your class path.

## Run a script in a MIDlet

For MIDlets the `JmePlatform` is used instead:

```java
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jme.*;

Globals globals = JmePlatform.standardGlobals();
LuaValue chunk = globals.loadfile("examples/lua/hello.lua");
chunk.call();
```

The file must be a resource within within the midlet jar for the loader to find it.
Any files included via `require()` must also be part of the midlet resources.

A simple example may be found in
[examples/jme/SampleMIDlet.java](examples/jme/SampleMIDlet.java)

You must include the library **luaj-jme-3.0.2.jar** in your midlet jar.

You must install the wireless toolkit and define `WTK_HOME` for this script to work. 

## Run a script using JSR-223 Dynamic Scripting


The standard use of JSR-223 scripting engines may be used:
```java
ScriptEngineManager mgr = new ScriptEngineManager();
ScriptEngine e = mgr.getEngineByName("luaj");
e.put("x", 25);
e.eval("y = math.sqrt(x)");
System.out.println( "y="+e.get("y") );
```

You can also look up the engine by language "lua" or mimetypes "text/lua" or "application/lua".

All standard aspects of script engines including compiled statements are supported.

You must include the library **luaj-jse-[version].jar** in your class path.

A working example may be found in
[examples/jse/ScriptEngineSample.java](examples/jse/ScriptEngineSample.java)

To compile and run it using Java 21 or higher:

```shell
javac -cp luaj-jse-[version].jar examples/jse/ScriptEngineSample.java
java -cp &quot;luaj-jse-[version].jar;examples/jse&quot; ScriptEngineSample
```

## Excluding the lua bytecode compiler

By default, the compiler is included whenever `standardGlobals()` or <`>debugGlobals()` are called.  
Without a compiler, files can still be executed, but they must be compiled elsewhere beforehand.
The "luac" utility is provided in the jse jar for this purpose, or a standard lua compiler can be used.

To exclude the lua-to-lua-bytecode compiler, do not call 
`standardGlobals()` or `debugGlobals()` 
but instead initialize globals with including only those libraries 
that are needed and omitting the line:
```java
org.luaj.vm2.compiler.LuaC.install(globals);
```

## Including the LuaJC lua-bytecode-to-Java-bytecode compiler

To compile from lua to Java bytecode for all lua loaded at runtime, 
install the LuaJC compiler into a `globals` object use:
```java
org.luaj.vm2.jse.luajc.LuaJC.install(globals);
```

This will compile all lua bytecode into Java bytecode, regardless of if they are loaded as
lua source or lua binary files.

The requires `bcel` to be on the class path, and the ClassLoader of JSE or CDC.  

# 3 - Concepts
## Globals

The old notion of platform has been replaced with creation of globals.  
The  [Globals](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/Globals.html)
class holds global state needed for executing closures as well as providing
convenience functions for compiling and loading scripts.

## Platform

To simplify construction of Globals, and encapsulate differences needed to support
the diverse family of Java runtimes, luaj uses a Platform notion.  
Typically, a platform is used to construct a Globals, which is then provided as a global
environment for client scripts.

### JsePlatform

The [JsePlatform](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/lib/jse/JsePlatform.html)
class can be used as a factory for globals in a typical Java SE application.
All standard libraries are included, as well as the luajava library.
The default search path is the current directory,
and the math operations include all those supported by Java SE.

#### Android

Android applications should use the JsePlatform, and can include the [luajava](#luajava) library
to simplify access to underlying Android APIs.  
A specialized Globals.finder should be provided to find scripts and data for loading.
See [examples/android/src/android/LuajView.java](examples/android/src/android/LuajView.java)
for an example that loads from the "res" Android project directory.

#### Applet

Applets in browsers should use the JsePlatform. The permissions model in applets is
highly restrictive, so a specialization of the [Luajava](#luajava) library must be used that
uses default class loading. This is illustrated in the sample Applet
[examples/jse/SampleApplet.java](examples/jse/SampleApplet.java),
which can be built using [examples/jse/SampleApplet.java](examples/jse/SampleApplet.java)[build-applet.xml](build-applet.xml).

### JmePlatform

The JmePlatform class can be used to set up the basic environment for a Java ME application.
The default search path is limited to the jar resources,
and the math operations are limited to those supported by Java ME.
All libraries are included except luajava, and the os, io, and math libraries are
limited to those functions that can be supported on that platform.

#### MIDlet

MIDlets require the JmePlatform.  
The JME platform has several limitations which carry over to luaj.
In particular Globals.finder is overridden to load as resources, so scripts should be
colocated with class files in the MIDlet jar file.  [Luajava](#luajava) cannot be used.
Camples code is in  
[examples/jme/SampleMIDlet.java](examples/jme/SampleMIDlet.java).

## Thread Safety

Luaj 3.0 can be run in multiple threads, with the following restrictions:
*   Each thread created by client code must be given its own, distinct Globals instance
*   Each thread must not be allowed to access Globals from other threads
*   Metatables for Number, String, Thread, Function, Boolean, and and Nil are shared and therefore should not be mutated once lua code is running in any thread.

For an example of loading allocating per-thread Globals and invoking scripts in
multiple threads see [examples/jse/SampleMultiThreaded.java](examples/jse/SampleMultiThreaded.java)

As an alternative, the JSR-223 scripting interface can be used, and should always provide a separate Globals instance 
per script engine instance by using a ThreadLocal internally.  

## Sandboxing

Lua and luaj are allowing for easy sandboxing of scripts in a server environment.

Considerations include
*   The `debug` and `luajava` library give unfettered access to the luaj vm and java vm so can be abused
*   Portions of the `os`, `io`, and `coroutine` libraries are prone to abuse
*   Rogue scripts may need to be throttled or killed
*   Shared metatables (string, booleans, etc.) need to be made read-only or isolated via 
class loaders such as [LuajClassLoader](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/server/LuajClassLoader.html)

Luaj provides sample code covering various approaches:
*   [examples/jse/SampleSandboxed.java](examples/jse/SampleSandboxed.java) A java sandbox that limits libraries, limits bytecodes per script, and makes shared tables read-only
*   [examples/jse/samplesandboxed.lua](examples/lua/samplesandboxed.lua) A lua sandbox that limits librares,limits bytecodes per script, and makes shared tables read-only
*   [examples/jse/SampleUsingClassLoader.java](examples/jse/SampleUsingClassLoader.java) A heavier but strong sandbox where each script gets its own class loader and a full private luaj implementation

# 4 - Libraries
## Standard Libraries

Libraries are coded to closely match the behavior specified in
See [standard lua documentation](http://www.lua.org/manual/5.1/) for details on the library API's


The following libraries are loaded by both `JsePlatform.standardGlobals()` and `JmePlatform.standardGlobals()`:
```	
base
bit32
coroutine
io
math
os
package
string
table
```

The `JsePlatform.standardGlobals()` globals also include:
```	
luajava 
```

The `JsePlatform.debugGlobals()` and `JsePlatform.debugGlobals()` functions produce globals that include:
```	
debug
```

### I/O Library

The implementation of the `io` library differs by platform owing to platform limitations.

The `JmePlatform.standardGlobals()` instantiated the io library `io` in 
```
src/jme/org/luaj/vm2/lib/jme/JmeIoLib.java
```

The `JsePlatform.standardGlobals()` includes support for random access and is in
```
src/jse/org/luaj/vm2/lib/jse/JseIoLib.java
```

### OS Library

The implementation of the `os` library also differs per platform.

The basic `os` library implementation us used by `JmePlatform` and is in:
```
src/core/org/luaj/lib/OsLib.java
```

A richer version for use by `JsePlatform` is :
```
src/jse/org/luaj/vm2/lib/jse/JseOsLib.java
```

Time is a represented as number of seconds since the epoch,
and locales are not implemented.

### Coroutine Library

The `coroutine` library is implemented using one JavaThread per coroutine.   
This allows `coroutine.yield()` can be called from anywhere,
as with the yield-from-anywhere patch in C-based lua.

Luaj uses WeakReferences and the OrphanedThread error to ensure that coroutines that are no longer referenced 
are properly garbage collected.  For thread safety, OrphanedThread should not be caught by Java code.  
See [LuaThread](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/LuaThread.html) 
and [OrphanedThread](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/OrphanedThread.html) javadocs for details. 
The sample code in [examples/jse/CollectingOrphanedCoroutines.java](examples/jse/CollectingOrphanedCoroutines.java) provides working examples.

### Debug Library

The `debug` library is not included by default by
`JmePlatform.standardGlobals()` or `JsePlatform.standardGlobsls()` .

The functions `JmePlatform.debugGlobals()` and `JsePlatform.debugGlobsls()`
create globals that contain the debug library in addition to the other standard libraries.

To install dynamically from lua use java-class-based require:`:
```lua 
require 'org.luaj.vm2.lib.DebugLib'
```

The `lua` command line utility includes the `debug` library by default.

### Luajava
The `JsePlatform.standardGlobals()` includes the `luajava` library, which simplifies binding to Java
classes and methods.  
It is patterned after the original [luajava project](http://www.keplerproject.org/luajava/).

The following lua script will open a swing frame on Java SE:
```lua 
jframe = luajava.bindClass( "javax.swing.JFrame" );
frame = luajava.newInstance( "javax.swing.JFrame", "Texts" );
frame:setDefaultCloseOperation(jframe.EXIT_ON_CLOSE);
frame:setSize(300,400);
frame:setVisible(true);
```

See a longer sample in `examples/lua/swingapp.lua` for details, including a simple animation loop, rendering graphics, mouse and key handling, and image loading. 
Or try running it using: 
```shell
java -cp luaj-jse-[version].jar lua examples/lua/swingapp.lua
```

The Java ME platform does not include this library, and it cannot be made to work because of the lack of a reflection API in Java ME. 

The `lua` connand line tool includes `luajava`. 

# 5 - LuaJ API

## API Javadoc

The javadocs for the core can be found [here](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-core/3.0.3)
and for the JSE [here](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3).

You can also build a local version from sources using
```shell
mvn clean javadoc:jar
```

## LuaValue and Varargs

All lua value manipulation is now organized around
[LuaValue](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/LuaValue.html) 
which exposes the majority of interfaces used for lua computation.

### Common Functions

`LuaValue` exposes functions for each of the operations in LuaJ.  
Some commonly used functions and constants include:
```java
call();               // invoke the function with no arguments
call(LuaValue arg1);  // call the function with 1 argument
invoke(Varargs arg);  // call the function with variable arguments, variable return values
get(int index);       // get a table entry using an integer key
get(LuaValue key);    // get a table entry using an arbitrary key, may be a LuaInteger
rawget(int index);    // raw get without metatable calls
valueOf(int i);       // return LuaValue corresponding to an integer
valueOf(String s);    // return LuaValue corresponding to a String
toint();              // return value as a Java int
tojstring();          // return value as a Java String
isnil();              // is the value nil
NIL;                  // the value nil
NONE;                 // a Varargs instance with no values	 
```

## Varargs

The interface[Varargs](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/Varargs.html) provides an abstraction for
both a variable argument list and multiple return values.  
For convenience, `LuaValue` implements `Varargs` so a single value can be supplied anywhere
variable arguments are expected.

### Common Functions

`Varargs` exposes functions for accessing elements, and coercing them to specific types:
```java
narg();                 // return number of arguments
arg1();                 // return the first argument
arg(int n);             // return the nth argument
isnil(int n);           // true if the nth argument is nil
checktable(int n);      // return table or throw error
optlong(int n,long d);  // return n if a long, d if no argument, or error if not a long
```

See the [Varargs](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/Varargs.html) API for a complete list.

## LibFunction

The simplest way to implement a function is to choose a base class based on the number of arguments to the function.
LuaJ provides 5 base classes for this purpose, depending on if the function has 0, 1, 2, 3 or variable arguments,
and if it provides multiple return values.
- [org.luaj.vm2.lib.ZeroArgFunction](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/lib/ZeroArgFunction.html)
- [org.luaj.vm2.lib.TwoArgFunction](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/lib/TwoArgFunction.html)
- [org.luaj.vm2.lib.OneArgFunction](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/lib/OneArgFunction.html)
- [org.luaj.vm2.lib.ThreeArgFunction](https://repo.gamecrash.dev/javadoc/releases/org/luaj/-jse/3.0.3/raw/org/luaj/vm2/lib/ThreeArgFunction.html)
- [org.luaj.vm2.lib.VarArgFunction](https://repo.gamecrash.dev/javadoc/releases/org/luaj/-jse/3.0.3/raw/org/luaj/vm2/lib/VarArgFunction.html)

Each of these functions has an abstract method that must be implemented,
and argument fixup is done automatically by the classes as each Java function is invoked.

An example of a function with no arguments but a useful return value might be:
```java
public class hostname extends ZeroArgFunction {
	public LuaValue call() {
		return valueOf(java.net.InetAddress.getLocalHost().getHostName());
	}
}
```

The value `env` is the environment of the function, and is normally supplied
by the instantiating object whenever default loading is used.

Calling this function from lua could be done by:
```lua
local hostname = require( 'hostname' )
```

while calling this function from Java would look like:
```lua
new hostname().call();
```

Note that in both the lua and Java case, extra arguments will be ignored, and the function will be called.  
Also, no virtual machine instance is necessary to call the function.
To allow for arguments, or return multiple values, extend one of the other base classes.

## Libraries of Java Functions

When require() is called, it will first attempt to load the module as a Java class that implements LuaFunction.  
To succeed, the following requirements must be met:
*   The class must be on the class path with name, `modname`.
*   The class must have a public default constructor.
*   The class must inherit from LuaFunction.

If luaj can find a class that meets these critera, it will instantiate it, cast it to `LuaFunction` 
then call() the instance with two arguments: 
the `modname` used in the call to require(), and the environment for that function.  
The Java may use these values however it wishes. A typical case is to create named functions 
in the environment that can be called from lua. 

A complete example of Java code for a simple toy library is in [examples/jse/hyperbolic.java](examples/jse/hyperbolic.java)
```java
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.*;

public class hyperbolic extends TwoArgFunction {

	public hyperbolic() {}

	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaValue library = tableOf();
		library.set( "sinh", new sinh() );
		library.set( "cosh", new cosh() );
		env.set( "hyperbolic", library );
		return library;
	}

	static class sinh extends OneArgFunction {
		public LuaValue call(LuaValue x) {
			return LuaValue.valueOf(Math.sinh(x.checkdouble()));
		}
	}
	
	static class cosh extends OneArgFunction {
		public LuaValue call(LuaValue x) {
			return LuaValue.valueOf(Math.cosh(x.checkdouble()));
		}
	}

}
```

In this case the call to require invokes the library itself to initialize it. The library implementation
puts entries into a table, and stores this table in the environment.


The lua script used to load and test it is in [examples/lua/hyperbolicapp.lua](examples/lua/hyperbolicapp.lua)
```lua
require 'hyperbolic'

print('hyperbolic', hyperbolic)
print('hyperbolic.sinh', hyperbolic.sinh)
print('hyperbolic.cosh', hyperbolic.cosh)

print('sinh(0.5)', hyperbolic.sinh(0.5))
print('cosh(0.5)', hyperbolic.cosh(0.5))
```

For this example to work the code in `hyperbolic.java` must be compiled and put on the class path.

## Closures

Closures still exist in this framework, but are optional, and are only used to implement lua bytecode execution,
and is generally not directly manipulated by the user of luaj.

See the [org.luaj.vm2.LuaClosure](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/LuaClosure.html)
javadoc for details on using that class directly. 

# 6 - Parser

## Javacc Grammar

A Javacc grammar was developed to simplify the creation of Java-based parsers for the lua language.
The grammar is specified for [javacc version 5.0](https://javacc.dev.java.net/) because that tool generates
standalone
parsers that do not require a separate runtime.

A plain undecorated grammer that can be used for validation is available in
[grammar/Lua52.jj](http://luaj.org/luaj/3.0/grammar/Lua52.jj) while a grammar that generates a typed parse tree is in 
[grammar/LuaParser.jj](http://luaj.org/luaj/3.0/grammar/LuaParser.jj)

## Creating a Parse Tree from Lua Source
The default lu compiler does a single-pass compile of lua source to lua bytecode, so no explicit parse tree is produced.

To simplify the creation of abstract syntax trees from lua sources, the LuaParser class is generated as part of the JME build.  
To use it, provide an input stream, and invoke the root generator, which will return a Chunk if the file is valid, 
or throw a ParseException if there is a syntax error.


For example, to parse a file and print all variable names, use code like:
```java
try {
    String file = "main.lua";
    LuaParser parser = new LuaParser(new FileInputStream(file));
    Chunk chunk = parser.Chunk();
    chunk.accept( new Visitor() {
        public void visit(Exp.NameExp exp) {
            System.out.println("Name in use: "+exp.name.name
                +" line "+exp.beginLine
                +" col "+exp.beginColumn);
        }
    } );
} catch ( ParseException e ) {
    System.out.println("parse failed: " + e.getMessage() + "n"
        + "Token Image: '" + e.currentToken.image + "'n"
        + "Location: " + e.currentToken.beginLine + ":" + e.currentToken.beginColumn 
                 + "-" + e.currentToken.endLine + "," + e.currentToken.endColumn);
}
``` 

An example that prints locations of all function definitions in a file may be found in
[examples/jse/SampleParser.java](examples/jse/SampleParser.java) 

See the [org.luaj.vm2.ast package](https://repo.gamecrash.dev/javadoc/releases/org/luaj/luaj-jse/3.0.3/raw/org/luaj/vm2/ast/package-summary.html) javadoc 
for the API 
relating to the syntax tree that is produced.

# 7 - Building and Testing
## Maven integration

The main jar files are deployed to a selfhosted maven repository:
```xml
<repositories>
	<repository>
		<id>gitea</id>
		<url>https://git.gamecrash.dev/api/packages/game.crash/maven</url>
	</repository>
</repositories>
```

For JSE projects, add this dependency for the luaj-jse jar:
```xml
<dependency>
    <groupId>org.luaj</groupId>
    <artifactId>luaj-jse</artifactId>
    <version>3.0.3</version>
</dependency>
```

while for JME projects, use the luaj-jme jar:
```xml
<dependency>
	<groupId>org.luaj</groupId>
	<artifactId>luaj-jme</artifactId>
	<version>3.0.3</version>
</dependency>
```

An example skelton maven pom file for a skeleton project is in
[examples/maven/pom.xml](examples/maven/pom.xml)

## Building the jars
For building, maven is used. To build the entire project, you can use
````shell
mvn clean package
````

# 8 - Downloads
## Downloads and Project Pages

The specific packages for this project can be found [here](https://git.gamecrash.dev/game.crash/luaj/packages).
The available versions for the subpackages can then be found under the tab for that specific package - 
there are also the sources and javadoc jar files for download.

# 9 - Release Notes
## Main Changes by Version

|           |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **2.0**   | *   Initial release of 2.0 version                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| **2.0.1** | *   Improve correctness of singleton construction related to static initialization<br>*   Fix nan-related error in constant folding logic that was failing on some JVMs<br>*   JSR-223 fixes: add META-INF/services entry in jse jar, improve bindings implementation                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| **2.0.2** | *   JSR-223 bindings change: non Java-primitives will now be passed as LuaValue<br>*   JSR-223 enhancement: allow both ".lua" and "lua" as extensions in getScriptEngine()<br>*   JSR-223 fix: use system class loader to support using luaj as JRE extension<br>*   Improve selection logic when binding to overloaded functions using luajava<br>*   Enhance javadoc, put it [in distribution](docs/api/index.html) and at [http://luaj.sourceforge.net/api/2.0/](http://luaj.sourceforge.net/api/2.0/index.html)<br>*   Major refactor of luajava type coercion logic, improve method selection.<br>*   Add luaj-sources-2.0.2.jar for easier integration into an IDE such as Netbeans                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **2.0.3** | *   Improve coroutine state logic including let unreferenced coroutines be garbage collected<br>*   Fix lua command vararg values passed into main script to match what is in global arg table<br>*   Add arithmetic metatag processing when left hand side is a number and right hand side has metatable<br>*   Fix load(func) when mutiple string fragments are supplied by calls to func<br>*   Allow access to public members of private inner classes where possible<br>*   Turn on error reporting in LuaParser so line numbers ar available in ParseException<br>*   Improve compatibility of table.remove()<br>*   Disallow base library setfenv() calls on Java functions                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| **3.0**   | *   Convert internal and external API's to match lua 5.2.x environment changes<br>*   Add bit32 library<br>*   Add explicit Globals object to manage global state, especially to imrpove thread safety<br>*   Drop support for lua source to java surce (lua2java) in favor of direct java bytecode output (luajc)<br>*   Remove compatibility functions like table.getn(), table.maxn(), table.foreach(), and math.log10()<br>*   Add ability to create runnable jar file from lua script with sample build file build-app.xml<br>*   Supply environment as second argument to LibFunction when loading via require()<br>*   Fix bug 3597515 memory leak due to string caching by simplifying caching logic.<br>*   Fix bug 3565008 so that short substrings are backed by short arrays.<br>*   Fix bug 3495802 to return correct offset of substrings from string.find().<br>*   Add artifacts to Maven central repository.<br>*   Limit pluggable scripting to use compatible bindings and contexts, implement redirection.<br>*   Fix bug that didn't read package.path from environment.<br>*   Fix pluggable scripting engine lookup, simplify implementation, and add unit tests.<br>*   Coerce script engine eval() return values to Java.<br>*   Fix Lua to Java coercion directly on Java classes.<br>*   Fix Globals.load() to call the library with an empty modname and the globals as the environment.<br>*   Fix hash codes of double.<br>*   Fix bug in luajava overload resolution.<br>*   Fix luastring bug where parsing did not check for overflow.<br>*   Fix luastring bug where circular dependency randomly caused NullPointerException.<br>*   Major refactor of table implementation.<br>*   Improved behavior of next() (fixes issue #7).<br>*   Existing tables can now be made weak (fixes issue #16).<br>*   More compatible allocation of table entries in array vs. hash (fixes issue #8).<br>*   Fix os.time() to return a number of seconds instead of milliseconds.<br>*   Implement formatting with os.date(), and table argument for os.time().<br>*   LuaValue.checkfunction() now returns LuaFunction.<br>*   Refactor APIs related to compiling and loading scripts to provide methods on Globals.<br>*   Add API to compile from Readers as well as InputStreams.<br>*   Add optional -c encoding flag to lua, luac, and luajc tools to control source encoding.<br>*   Let errors thrown in debug hooks bubble up to the running coroutine.<br>*   Make error message handler function in xpcall per-thread instead of per-globals.<br>*   Establish "org.luaj.debug" and "org.luaj.luajc" system properties to configure scripting engine.<br>*   Add sample code for Android Application that uses luaj.<br>*   Add sample code for Applet that uses luaj.<br>*   Fix balanced match for empty string (fixes issue #23).<br>*   Pass user-supplied ScriptContext to script engine evaluation (fixes issue #21).<br>*   Autoflush and encode written bytes in script contexts (fixes issue #20).<br>*   Rename Globals.FINDER to Globals.finder.<br>*   Fix bug in Globals.UTF8Stream affecting loading from Readers (fixes issue #24).<br>*   Add buffered input for compiling and loading of scripts.<br>*   In CoerceJavaToLua.coerse(), coerce byte[] to LuaString (fixes issue #31).<br>*   In CoerceJavaToLua.coerse(), coerce LuaValue to same value (fixes issue #29).<br>*   Fix line number reporting in debug stack traces (fixes issue #30). |
| **3.0.1** | *   Fix __len metatag processing for tables.<br>*   Add fallback to __lt when pocessing __le metatag.<br>*   Convert anonymous classes to inner classes (gradle build support).<br>*   Allow error() function to pass any lua object including non-strings.<br>*   Fix string backing ownership issue when compiling many scripts.<br>*   Make LuaC compile state explicit and improve factoring.<br>*   Add sample build.gradle file for Android example.<br>*   collectgarbage() now behaves same as collectgarbage("collect") (fixes issue #41).<br>*   Allow access to Java inner classes using lua field syntax (fixes issue #40).<br>*   List keyeq() and keyindex() methods as abstract on LuaTable.Entry (issue #37).<br>*   Fix return value for table.remove() and table.insert() (fixes issue #39)<br>*   Fix aliasing issue for some multiple assignments from varargs return values (fixes issue #38)<br>*   Let os.getenv() return System.getenv() values first for JSE, then fall back to properties (fixes issue #25)<br>*   Improve garbage collection of orphaned coroutines when yielding from debug hook functions (fixes issue #32).<br>*   LuaScriptEngineFactory.getScriptEngine() now returns new instance of LuaScriptEngine for each call.<br>*   Fix os.date("*t") to return hour in 24 hour format (fixes issue #45)<br>*   Add SampleSandboxed.java example code to illustrate sandboxing techniques in Java.<br>*   Add samplesandboxed.lua example code to illustrate sandboxing techniques in lua.<br>*   Add CollectingOrphanedCoroutines.java example code to show how to deal with orphaned lua threads.<br>*   Add LuajClassLoader.java and Launcher.java to simplify loading via custom class loader.<br>*   Add SampleUsingClassLoader.java example code to demonstrate loading using custom class loader.<br>*   Make shared string metatable an actual metatable.<br>*   Add sample code that illustrates techniques in creating sandboxed environments.<br>*   Add convenience methods to Global to load string scripts with custom environment.<br>*   Move online docs to [http://luaj.org/luaj/3.0/api/](http://luaj.org/luaj/3.0/api/index.html)<br>*   Fix os.time() conversions for pm times.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| **3.0.2** | *   Fix JsePlatform.luaMain() to provide an "arg" table in the chunk's environment.<br>*   Let JsePlatform.luaMain() return values returned by the main chunk.<br>*   Add synchronization to CoerceJavaToLua.COERCIONS map.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |

## Known Issues
### Limitations
*   debug code may not be completely removed by some obfuscators
*   tail calls are not tracked in debug information
*   mixing different versions of luaj in the same java vm is not supported
*   values associated with weak keys may linger longer than expected
*   behavior of luaj when a SecurityManager is used has not been fully characterized
*   negative zero is treated as identical to integer value zero throughout luaj
*   lua compiled into java bytecode using luajc cannot use string.dump() or xpcall()
*   number formatting with string.format() is not supported
*   shared metatables for string, bool, etc are shared across Globals instances in the same class loader
*   orphaned threads will not be collected unless garbage collection is run and sufficient time elapses

### File Character Encoding
Source files can be considered encoded in UTF-8 or ISO-8859-1 and results should be as expected, 
with literal string contianing quoted characters compiling to the same byte sequences as the input. 

For a non ASCII-compatible encoding such as EBSDIC, however, there are restrictions:
*   supplying a Reader to Globals.load() is preferred over InputStream variants
*   using FileReader or InputStreamReader to get the default OS encoding should work in most cases
*   string literals with quoted characters may not produce the expected values in generated code
*   command-line tools lua, luac, and luajc will require `-c Cp037` to specify the encoding
These restrictions are mainly a side effect of how the language is defined as allowing byte literals
within literal strings in source files.

Code that is generated on the fly within lua and compiled with lua's `load()` function
should work as expected, since these strings will never be represented with the
host's native character encoding.