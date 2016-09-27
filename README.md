# jsearch
RI Challenge - http://www.wladmirbrandao.com/course/cs-bsc-irs_RIChallenge_201602.pdf

### Tech

[Apache Lucene] - The Apache LuceneTM project develops open-source search software.

### Requirements

* Linux or Mac OSX
* OpenJDK 1.8+ or Oracle JDK 1.8+


### Run

Put data inside "data" folder in project root.

```sh
$ git clone https://github.com/josuesasilva/jsearch.git
$ cd jsearch
$ mkdir data # put collection here
```

```sh
$ java -jar jsearch.jar -q userquery
```
or

```sh
$ java -jar jsearch.jar -f queriesFile
```

### Results

Inside release folder contains Trec Eval binary and results for all supported IR models.

### Build

Load project with with Netbeans IDE.

