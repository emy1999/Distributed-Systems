Αλχίρχ Πάντια-Μαρίνα (3170005)
Δανοπούλου Έμυ (3170033)
Καλιαμούτος Παύλος (3160046)
Μπαλή Νίκη (3170114)
Χαβιατζή Ελένη (3170172)


Πριν τρέξει το πρόγραμμα:

Στην κλάση PublisherNode η μεταβλητή path περιέχει την τοποθεσία που είναι αποθηκευμένο το dataset με τα τραγούδια, 
οπότε πριν τρέξει το πρόγραμμα χρειάζεται να τροποποιηθεί κατάλληλα (line 28 στην κλάση PublisherNode).

Στις κλάσεις BrokerNode, PublisherNode και ConsumerNode αλλάζουμε τις IP addresses που έχουν δηλωθεί όπως επιθυμούμε
(lines 465,466,467 στο BrokerNode, 
lines 36,37,38,441,442 στο PublisherNode, 
lines 74,182 στο ConsumerNode).

Για την εργασία χρησιμοποιήσαμε το dataset1.

To mp3agic-0.9.1.jar πρέπει να περαστεί ως library στο project.
Χρησιμοποιύμε μέθοδο (isBlank()), που χρειάζεται τουλάχιστον jdk 11.

Στο Android Studio project μας αλλάζουμε την IP address στην κλάση Main Activity (lines 118,131).

Τρέχοντας το πρόγραμμα:

Αρχικά τρέχουμε την κλάση BrokerNode 3 φορές, όπου δημιουργούνται 3 διαφορετικά processes για τον κάθε broker.

Στη συνέχεια τρέχουμε την κλάση PublisherNode 2 φορές, όπου δημιουργούνται 2 διαφορετικά processes για τον κάθε publisher.
Ο ένας publisher είναι υπεύθυνος για καλλιτέχνες με ονόματα που αρχίζουν από Α και μέχρι αυτά που αρχίζουν από Μ, και ο άλλος publisher είναι υπεύθυνος για όλα τα υπόλοιπα. Ο κάθε publisher συνδέεται και με τους τρεις brokers.

Τέλος, τρέχουμε την εφαρμογή μας στο Android Studio (χρησιμοποιούμε τον emulator Pixel 2 API 7).
