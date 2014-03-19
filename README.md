![rsine](https://raw.github.com/rsine/rsine/devel/src/main/resources/rsine_transparent.png "rsine")

## About

**Rsine** (Resource SubscrIption and Notification sErvice) is a service that allows users to subscribe for notification
on specific changes to triples in an RDF dataset. It is developed as part of work package 5 in the [LOD2 project](http://lod2.eu/WikiArticle/Project.html)
 by [Semantic Web Company](http://www.semantic-web.at/).

Rsine is implemented as a service that listens for changes of triples (additions/removals) stored in an RDF triple store
(the *managed store*) which is accessibly by a SPARQL endpoint. Users can express their interest in data changes as subscriptions
 which are essentially SPARQL queries that are run against the history of changes and the managed store. A subscription
 consists of

 * definition of the type of change (e.g., addition of a triple with property skos:broader)
 * an *optional* condition that must be fulfilled for receiving the notification
 * a pattern that defines the text and data values contained in the notification message
 * a notifier that specifies the way the notification is disseminated to the subscriber (e.g, by email)

Rsine takes care of issuing the queries, assembling the notification message and disseminating it to the subscriber. Rsine
relies on getting information about the triples written to or removed from the managed store. Currently we support two storage
solutions for RDF data:

 * [Virtuoso](http://virtuoso.openlinksw.com/): To establish interoperability with rsine, the provided
 [vad package](https://github.com/rsine/rsineVad) must installed
 * [openRDF](http://www.openrdf.org/): We developed a reference implementation of a <tt>RepositoryConnectionListener</tt> that forwards
 triple changes to rsine. The implementation is not published yet.

### Example Use-case

Development of controlled vocabularies (e.g., thesauri) is typically a manual and error-prone process. [SKOS](http://www.w3.org/2004/02/skos/)
has been widely accepted as a schema to to publish thesauri on the Web of Data. During development, vocabularies tend to become
increasingly complex and in many cases they are edited and curated by more than one person. However, the [SKOS reference](http://www.w3.org/TR/skos-reference/)
defines a number of basic integrity constraints that should be met and which can easily be missed. Such constraints encompass, e.g.,

 * Non-disjoint labels (One concept has identical preferred and alternative labels)
 * Relation clashes (Hierarchically related concepts are also associatively related)
 * Mapping clashes (Concepts mapped by skos:exactMatch must not be also related by a hierarchical mapping property)

Furthermore, custom vocabulary or use-case-specific constraints could be specified. Rsine is able to cover the constraints
outlined above and is, of course, capable to notify subscribers of any number of additional custom-defined constraints.
Also see the section *Integration Examples* below for additional information on rsine usage scenarios.

## Installation

Requirements:

 * Verify that Java version 1.7 or greater is installed: <tt>javac -version</tt>
 * Make sure Maven version 3.0 or greater is installed: <tt>mvn -v</tt>
 * Make sure you have the current version of the [git version control system](http://git-scm.com/) installed on your system

### Build from Source

#### Configuration before building (optional)

If you know in advance for what SPARQL endpoint you want to configure rsine, you can set this information in the
<tt>application.properties</tt> file, located in the <tt>rsine/src/main/resources</tt> directory. However, you can also
skip this step and set the relevant parameters at runtime (i.e. when starting the rsine service).

#### Performing the Build

1. Get the sourcecode by cloning the rsine repository: <tt>git clone https://github.com/rsine/rsine.git</tt>)
2. Change into the newly created <tt>rsine</tt> directory and build the application: <tt>mvn -DskipTests=true package</tt>

The file <tt>rsine-cmd.jar</tt> is now available in the directory <tt>rsine/target</tt>

Known Issues:

 * When building rsine with tests enabled (i.e. without the <tt>-DskipTests=true</tt> switch) it can happen that the build
fails due to non-successful tests. This happens when all tests are run consecutively and seems to be caused by some
concurrency issue in the test setup. As far as we know, these failing tests do not affect the functionality of the rsine
application.

## Usage

1. Change to the <tt>rsine/target</tt> directory
2. Run the tool using <tt>java -jar rsine-cmd.jar</tt>

### Configuration

As describe above, rsine reads it's configuration data from the file <tt>application.properties</tt> which is provided
at compile time. If you decided not to edit this file before compilation (see description above) you can set the most
essential parameters on the command line:

### Commandline Parameters

To get a synopsis on the supported parameters, type <tt>java -jar rsine-cmd.jar --help</tt>.

 * <tt>-s, --sparql-endpoint</tt>: The URI of the SPARQL endpoint where the managed store can be queried.
 * <tt>-p, --port</tt>: The port where rsine listens for connections (i.e., triple announcements and subscription requests).
 * <tt>-a, --authoritative-uri</tt>: This parameter needs to be provided in order to help rsine determine which resources
 are locally defined and managed (i.e. in the managed store) and which resources constitute an 'external' link. For example,
 if you develop a thesaurus in your managed store whose concept URIs all start with <tt>http://myvocabulary.org/concept/</tt>
 you would set this URI as the authoritative uri. Thus rsine can detect whenever you link to 'external' resources on the Web
 that resolve to different hosts. If you do not provide any value for this parameter, rsine will try to automatically
 detect it from the managed store sparql endpoint URI.

Summarizing, you are required to provide at least the SPARQL endpoint of your managed store (-s), all other parameters are
optional.

## Subscriptions

Subscriptions are RDF documents that are sent to <tt>http://{rsinehost}/register</tt> by HTTP post (<tt>{rsinehost}</tt>
being the host where the rsine service is running). A simple example can be viewed [here](https://raw.github.com/rsine/rsine/devel/src/test/resources/internal/emailNotifierSubscription.ttl), but also more [complex subscriptions](https://raw.github.com/rsine/rsine/devel/src/test/resources/quality/cyclic_hierarchical_relations.ttl) are possible.

### Components

Subscriptions contain of two mandatory parts: The *query* which specifies the resources the subscriber is interested to
 get notifications about and one or more *notifiers* that define the way notification messages should be disseminated. The
 basic structure looks like this:

1. Query
  * Changeset Selection
  * Condition (optional)
  * Auxiliary Query (optional)
  * Formatter (optional)
2. Notifier(s)

### Changeset Selection

A changeset selection is responsible for selecting the type of change a subscriber is interested in. It is a mandatory
component of the query part. The following example shows a changeset selection that states interest in all newly created (<tt>?cs cs:addition ?addition</tt>) preferred labels (<tt>?addition rdf:predicate skos:prefLabel</tt>) of a concept (<tt>?addition rdf:subject ?concept</tt>) and its value (<tt>?addition rdf:object ?newLabel</tt>).

```
rsine:query [
    spin:text "PREFIX cs:<http://purl.org/vocab/changeset/schema#>
        PREFIX spin:<http://spinrdf.org/sp/>
        PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
        SELECT ?concept ?newLabel WHERE {
            ?cs a cs:ChangeSet .
            ?cs cs:addition ?addition .
            ?addition rdf:subject ?concept .
            ?addition rdf:predicate skos:prefLabel .
            ?addition rdf:object ?newLabel
        }";
    ];
```

### Condition

In addition to getting notified on occurrence of certain changesets, in many cases it is necessary to further specify
 conditions that must be met for the notification to be triggered. Suppose, e.g., you want to check if two concepts
 are connected by a hierarchical cycle you can use the following condition rule in your subscription:

```
rsine:condition [
    spin:text "PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
        ASK {
            ?concept skos:broader+ ?otherConcept .
            ?otherConcept skos:broader+ ?concept
        }";
    rsine:expect true;
];
```

Conditions are SPARQL ASK queries that can access the bindings from the changeset selection. A condition is met if the
 query results in the same value as stated by <tt>rsine:expect</tt>.

### Auxiliary Query

When crafting your notification subscriptions it is useful to provide a human-readable message that will be delivered to
 the subscribers. In these messages you often need to refer to data that are not part of the triple selection queries (
 changeset selection and conditions). E.g., you want the notification message to be *concept 'cat' has been hierarchically
 related to concept 'carnivore'* you also need to access the concept's preferred labels. The way to this are auxiliary
 queries. They do not influence the decision process of whether or not a notification 'fires' but are intended to bind values
 for information that is otherwise important.

Auxiliary queries also have access to the bindings from the changeset selection.  The following code snippet demonstrates
 how to bind the concept's labels to a variable with auxiliary queries:

```
rsine:auxiliary [
    spin:text "PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
        SELECT ?conceptLabel WHERE {
            ?concept skos:prefLabel ?conceptLabel .
            FILTER(langMatches(lang(?conceptLabel), 'en'))
        }";
    spin:text "PREFIX skos:<http://www.w3.org/2004/02/skos/core#>
        SELECT ?otherConceptLabel WHERE {
            ?otherConcept skos:prefLabel ?otherConceptLabel .
            FILTER(langMatches(lang(?otherConceptLabel), 'en'))
        }";
];
```

### Formatter

In order to send meaningful messages to notification subscribers, rsine provides a way to define a template that holds
 the desired information. This can be done by defining <tt>rsine:formatter</tt>s in your subscription. Currently we provide
 the <tt>rsine:vtlFormatter</tt> that can access the bindings from the changeset selections, conditions and auxiliary queries
 using the [Apache Velocity Engine](http://velocity.apache.org/). For our *hierarchical cycle* example, the following
 snippet illustrates how to formulate such a message template:

```
rsine:formatter [
    a rsine:vtlFormatter;
    rsine:message "The concepts <a href='$bindingSet.getValue('concept')'>$bindingSet.getValue('conceptLabel').getLabel()</a> and
        <a href='$bindingSet.getValue('otherConcept')'>$bindingSet.getValue('otherConceptLabel').getLabel()</a> form a hierarchical cycle";
];
```

### Notifier

The components that are responsible for disseminating the generated messages to the users are defined by the property
 <tt>rsine:notifier</tt> in the subscription. Currently we support two notifiers: the <tt>rsine:loggingNotifier</tt> and
 the <tt>rsine:emailNotifier</tt>. Whereas the former is mainly intended for debugging purposes, the latter is capable
 to, as the name implies, deliver the notification messages to the provided email address.

Here is how to add the logging notifier to your subscription:

```
rsine:notifier [
    a rsine:loggingNotifier;
];
```

If you want the notifications to be sent out by email, you'll want to use something like this:

```
rsine:notifier [
    a rsine:emailNotifier;
    foaf:mbox <mailto:c.mader@myhost.at>
];
```

Also note that currently for the <tt>emailNotifier</tt> to work, it expects an SMTP host on localhost, accessible at port
 25 per default. However, this can be changed by manually editing the file <tt>application.properties</tt> (see Section 'Build from Source').

### Putting it All Together

#### Example Subscriptions

A working subscription that sends out proper notificatons whenever two concepts are hierarchically connected and form
 a cycle can be viewed [here](https://raw.github.com/rsine/rsine/devel/src/test/resources/quality/cyclic_hierarchical_relations.ttl).

#### Registering Subscriptions

Rsine accepts subscriptions via a HTTP post to the <tt>/register</tt> URI. So, if you run rsine locally this URI would
 be <tt>http://localhost:2221/register</tt>. For testing, you can use [curl](http://curl.haxx.se/) to register subscriptions, 
 e.g., with <tt>curl -X POST -d @cyclic_hierarchical_relations.ttl --header "Content-Type: text/turtle" http://localhost:2221/register</tt>.
 Currently all subscriptions are lost if rsine is shut down, so you will have to re-register them again on restart.

## Integration Examples

### qSKOS
[qSKOS](https://github.com/cmader/qSKOS/) is an open-source project that aims to identify potential quality problems ('quality issues') in SKOS vocabularies and provides a way to automatically check against a catalog of these quality issues. It features it's own API and is available as a standalone [Java application](https://github.com/cmader/qSKOS/releases/latest) as well as [Web application](http://qskos.poolparty.biz/login).

Some of the checks qSKOS performs have already been integrated into rsine to demonstrate how these technologies can complement each other.

### LOD2 Project

In the course of the [LOD2 project](http://lod2.eu/WikiArticle/Project.html), rsine is installed in an evaluation environment at [Wolters Kluwer Germany](http://www.wolterskluwer.de/). The goal is to evaluate the impact of integrating subscription/notification services in controlled vocabulary development processes with a focus on vocabulary quality. The work is currently ongoing and results will be published soon.

## Future Work
Work on rsine is not yet finished. Although we were able to showcase it's usefulness in the examples above, we plan to
extend our work in the following directions:

 * Notification queries simplification (Changeset Selection)
 * Redesign rsine service URIs to fully comply with the REST principles
 * Integration/utilization of stream reasoning technologies
 * Publish the rsine RDF subscription schema
 * GUI for creating subscriptions

## Publications
Coming soon:

 * LOD2 Deliverables D5.3.1 and D7.3
 * LOD2 Book

## Contributors

* Christian Mader ([@cmader](https://github.com/cmader))
* Jürgen Jakobitsch ([@turnguard](https://github.com/turnguard))
* Helmut Nagy ([@nagyhel](https://github.com/nagyhel))

## Copyright

Please see our [Contributor Agreement](https://raw.github.com/rsine/rsine/devel/rsine_contribution_agreement.pdf).