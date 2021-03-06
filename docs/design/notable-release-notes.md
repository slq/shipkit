Only **[notable releases](link)** are published to [Central repository](link) and [JCenter](link)!
All **[incremental releases](link)** are published to [Mockito's Bintray repository](link).
Release notes are automatically generated by [Mockito Continuous Delivery Pipeline](https://github.com/mockito/mockito/wiki/Continuous-Delivery-Overview).
[Thank you](link) for testing with Mockito!

### 2.7.0 - 2017-01-29

Authors: 5 (link to details rn), commits: 40 (link to commits), improvements: 12 (link to details rn), notable:

* New strict mocking API - MockitoSession [(#857)](https://github.com/mockito/mockito/issues/857)
* Fix TypeCache dead lock [(#902)](https://github.com/mockito/mockito/pull/902)
* Mockito 2.6.4 hangs on JDK 1.8.0_31 [(#892)](https://github.com/mockito/mockito/issues/892)
* Overridden abstract methods using generics are not longer called for partial mock (working with Mockito 1.10.19) [(#874)](https://github.com/mockito/mockito/issues/874)
* Update to Byte Buddy 1.6.4: Fixes bridge method resolution for generic types [(#891)](https://github.com/mockito/mockito/pull/891)
* Java 9 support, upgrading to Objenesis 2.5 [(#882)](https://github.com/mockito/mockito/issues/882)

### 2.6.1 - 2017-01-12

* Tim van der Lippe: Built-in support for Android, new 'mockito-android' artifact [(#872)](https://github.com/mockito/mockito/pull/872)
* Szczepan Faber: Mockito JUnit Runner supports strict stubbing [(#854)](https://github.com/mockito/mockito/pull/854)
* Brice Dutheil: Fixes #820 ReturnsArgAt to handle returning vararg as arrays [(#821)](https://github.com/mockito/mockito/pull/821)
* Brice Dutheil: When custom exception fillInstackTrace() returns null, Exception mock cannot work properly [(#866)](https://github.com/mockito/mockito/issues/866)
* Rafael Winterhalter: Updated to Byte Buddy 1.6.0 [(#864)](https://github.com/mockito/mockito/pull/864)

### 2.5.0 - 2016-12-28

* Szczepan Faber: Stopped publishing java source inside main jar [(#850)](https://github.com/mockito/mockito/pull/850)
* Szczepan Faber: JUnit rule strictness can be tweaked per test method [(#843)](https://github.com/mockito/mockito/pull/843)
* Rafael Winterhalter: Rename compiled MockMethodDispatcher class so that it works with Robolectric [(#847)](https://github.com/mockito/mockito/pull/847)
* Rafael Winterhalter: Update Byte Buddy to support recent Java 9 builds [(#852)](https://github.com/mockito/mockito/pull/852)
* Brice Dutheil: New ArgumentMatchers.matches(Pattern regex) method for matching regex [(#839)](https://github.com/mockito/mockito/pull/839)

### 2.4.0 - 2016-12-21

* Szczepan Faber: JUnit runner reports argument stubbing mismatches [(#808)](https://github.com/mockito/mockito/pull/808)
* jakobjo: Verification listeners [(#719)](https://github.com/mockito/mockito/pull/719)
* Brice Dutheil: Introduce validable answers [(#826)](https://github.com/mockito/mockito/pull/826)
* Rafael Winterhalter: StackOverflow occurs when mocking a HashMap with inline mockmaker [(#823)](https://github.com/mockito/mockito/pull/823)
* Brice Dutheil: Verification using After and ArgumentCaptor with Times or AtLeast methods returns unexpected size of captured values list [(#379)](https://github.com/mockito/mockito/issues/379)
* LiamClark: New verification listeners [(#719)](https://github.com/mockito/mockito/pull/719)
* Brice Dutheil: Enable annotation engine as plugin [(#811)](https://github.com/mockito/mockito/pull/811)

### 2.4.0 - 2016-12-21

* Szczepan Faber: JUnit runner reports argument stubbing mismatches [(#808)](https://github.com/mockito/mockito/pull/808)
* jakobjo: Verification listeners [(#719)](https://github.com/mockito/mockito/pull/719)
* Brice Dutheil: Introduce validable answers [(#826)](https://github.com/mockito/mockito/pull/826)
* Rafael Winterhalter: StackOverflow occurs when mocking a HashMap with inline mockmaker [(#823)](https://github.com/mockito/mockito/pull/823)
* Brice Dutheil: Verification using After and ArgumentCaptor with Times or AtLeast methods returns unexpected size of captured values list [(#379)](https://github.com/mockito/mockito/issues/379)
* LiamClark: New verification listeners [(#719)](https://github.com/mockito/mockito/pull/719)
* Brice Dutheil: Enable annotation engine as plugin [(#811)](https://github.com/mockito/mockito/pull/811)

### 2.3.0 - 2016-12-10

No code changes. No commits found.

### 2.2.5 - 2016-12-05

Authors: 1, commits: 5, improvements: 0

No notable improvements. No pull requests were referenced from [commits](link).

### 2.2.0 - 2016-10-09

* OSGI bundle problem - correct version specification syntax for bytebuddy [(#679)](https://github.com/mockito/mockito/pull/679)

### 2.1.0 - 2016-10-03 - Brand new Mockito 2

Mockito 2: even cleaner tests!!! THANK you for writing great tests with us, your patience waiting for v2, and kudos to fantastic gang of contributors!

For comprehensive overview of the brand new release see [What's new in Mockito 2](https://github.com/mockito/mockito/wiki/What%27s-new-in-Mockito-2) wiki page.