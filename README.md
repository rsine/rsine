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
 * [openRDF](http://www.openrdf.org/): We developed a reference implementation of a `RepositoryConnectionListener` that forwards
 triple changes to rsine. The implementation is not published yet.

### Example Use-case

Thesaurus management

## Installation

### Requirements

### Building from Source

## Usage

## Subscriptions

## Notifications

## Integration Examples

### qSKOS

### LOD2 Project

## Future Work

## Contributors

* Christian Mader ([@cmader](https://github.com/cmader))
* Jürgen Jakobitsch ([@turnguard](https://github.com/turnguard))
* Helmut Nagy ([@nagyhel](https://github.com/nagyhel))

## Copyright

Please see our [Contributor Agreement](https://raw.github.com/rsine/rsine/devel/rsine_contribution_agreement.pdf).