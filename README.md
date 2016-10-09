# jsearch

### Documentation

RI Challenge - http://www.wladmirbrandao.com/course/cs-bsc-irs_RIChallenge_201602.pdf
Technical Report - https://github.com/josuesasilva/jsearch/blob/master/relatorio_tecnico.pdf

Inside release folder contains Trec Eval binary and results for all supported IR models.

### Runtime Requirements

* Linux or Mac OSX
* OpenJDK 1.8+ or Oracle JDK 1.8+

### Dependencies

* [Apache Lucene] - The Apache LuceneTM project develops open-source search software.

* [Lucene Wordnet]- Loads the WordNet prolog file wn_s.pl into a thread-safe main-memory hash map that can be used for fast high-frequency lookups of synonyms for any given (lowercase) word string.


### Build Requirements

* OpenJDK 1.8+ or Oracle JDK 1.8+
* Netbeans IDE 8.0+

### Building

Load project with with Netbeans IDE.

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
