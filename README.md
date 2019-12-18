# confighelper
make modpacks with ease

#setup asm
 for loading in deob `-Dfml.coreMods.load=com.jredfox.confighelper.asm.Plugin`
 for compiling input this into your build.gradle
```
jar {
    manifest {
        attributes 'FMLCorePlugin': 'com.jredfox.confighelper.asm.Plugin',
        'FMLCorePluginContainsFMLMod': 'false',
	    'FMLAT': 'your_at.cfg'
    }
}
```


