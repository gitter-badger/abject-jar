abject-jar
=======

```
abject: contemptible; despicable; Obsolete; cast aside;
```

We have sbt assembly (https://github.com/sbt/sbt-assembly) which works perfectly in just about all situations. If you really need it there is sbt one-jar (https://github.com/sbt/sbt-onejar). So, why make another SBT fat jar plugin? I wish I didn't have to, and if you need to use it then you may be feeling the same way. Essentially it exists because certain parts of the hadoop eco system are built in very brittle ways, the net result being that we need to build certain projects in very specific ways. Here are some links that go into more detail about the issue:

* https://github.com/twitter/scalding/issues/751#issuecomment-38235248
* https://github.com/ConcurrentCore/cascading-hive/issues/5
* I thought the issue related to this: https://groups.google.com/forum/#!topic/cascading-user/45h5g8Cf9fY, but I was wrong

Very specifically the use case here was for maestro (https://github.com/CommBank/maestro), which now includes integration with Hive such that we don't need to try and integrate Oozie and Cascades together in some brittle manner. We would prefer to just use cascades for workflow management, which lead to the Hive integration. If you take a look at the Hive code you will see a large amount of reflection that hangs everything together, with lots of inherent assumptions. At the time Hive 0.10 was built there was a prevailing jar convention where by dependencies are included in the fat jar as jars in the /lib folder of the jar (jars within jars, I know). SBT Assembly departs from this convention and as a result prevented the Hive integration (exceptions referenced above). 

I spent many days debugging and blaming Scalding (sorry guys). I landed up manually building a jar in the legacy format and found that this fixed the issue. From this, abject-jar was born, a fat jar plugin for SBT that should need to exist, but because of a particular combination of versions and poor implementation assumptions, it does...

I really hope that this project will prove to be truly abject (obsolete) with coming upgrades of Hive in a few months from now. 

P.S. I did make an attempt at getting assembly working for this case: https://gist.github.com/quintona/9652666


Usage
------

add a resolver for the repo: http://commbank.artifactoryonline.com/commbank/repo/

add abject-jar as a dependency in `project/plugins.sbt`:

```scala
addSbtPlugin("au.com.cba.omnia" % "abject-jar" % "0.0.1")
```

To build a fat jar, simply run: 
```
./sbt abject-jar
```

