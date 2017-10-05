WEITERARBEIT:

Im Augenblick stehe ich vor dem Problem,
dass der Wechsel zwischen Normal-Mode und Demo-Mode "klemmt".

Soll heißen:

Ich bin im Demo-Mode und habe eine Task laufen.

Drücke ich die Back-Taste,  dann müsste ich die Task anhalten,
die Punkteliste löschen,
die alte Punkte liste restaurieren und
dann möglicherweise in der darunterliegenden View diese aktualisieren.

DAS IST ZU KOMPLIZIERT.

LÖSUNG:

Im Holder habe ich zwei LISTEN von Punkten.

Ich kann den Holder in ZWEI Modi versetzen:

holder.setNormalMode();
holder.setDemoMode();

Im Holder verwende ich folgenden Trick;

    // representing application global data
    private List<BezierPoint> userData;
    private List<BezierPoint> demoData;
    private List<BezierPoint> data;

// das muss synchronized sein !!!!!!

    holder.setNormalMode() {
        data = userData;
    }

    holder.setDemoMode() {
         data = demoData;
    }

    Möglicherweise mache ich beide Methoden synchronized ... da ich
    wiederum möglicherweise die eine oder andere Methode NICHT um UI Thread aufrufe ....

Dann müsste es ohne Probleme möglich sein, dass

im Normalmode irgendeine Liste sichtbar ist
diese auch dann sichtar ist bzw. bleibt, wenn sich irgend eine Aktivität darüber legt
bei Wechsel in den DemoMode eine zweite Liste zur Anzweige kommt
beim Wechsel zurück in den Normalmode wieder die alte Liste da ist

Wann immer man in den DemoMode wechselt, sollte / könnte man möglicherweise
die alte Demo-Liste leeren ......

// --------------------------------------------------------------------------------

Thread bzw. Demo Klasse:

Wie sieht das mit Start und Stopp aus ???
Ist anhalten der Demo nicht besser ?!?!?
Vielleicht doch nicht ....

// --------------------------------------------------------------------------------

IN der Holder Klasse müsste meines Erachtens ÜERALL ein static hin ..........

Sonst schaut das Ganze irgendwie SCHIEF aus .......

// --------------------------------------------------------------------------------

VORSICHT: Habe Demo-Mode (in zwei Dateien strings.xm scharf geschalten !!!!)


// --------------------------------------------------------------------------------

Ohhhh - noch ein Bug: Wenn ich die App "ikonisiere",
dann werden die beiden Schaltflächen "Konstruktion" ind "Gitternetzlinien"
nicht wie beim letzten Mal gesetzt .........

// --------------------------------------------------------------------------------

Ohhhh - noch ein Bug: Wenn ich mit der "linken" Taste die App in den "Liste aller Tasks" Mode versetze,
dann läuft nach Wiederanwahl die Task NICHT WEITER ?!?!?!?!?!? FEHLER !!!!!!!!!!!!!

// --------------------------------------------------------------------------------



Irgendwie geht bei großen Gitternetzen das Verschieben NICHT ?!?!?!?


// --------------------------------------------------------------------------------

TODO: WICHTIG:

Die Demontration-View hat eine andere Größe ....
Da muss die CellLength geändert werden !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

// --------------------------------------------------------------------------------

Es müsste die Toolbar eingeführt werden.

Damit ich beim Rücksprung in den entsprechenden Events auch
den Holder löschen kann.

// --------------------------------------------------------------------------------

Fehler: Die Demonstration-View muss nach der Rückkehr
die Liste der Punkte löschen

// --------------------------------------------------------------------------------

Allgemein: Wann muss die Liste der Punkte eigentlich gelöscht werden

// --------------------------------------------------------------------------------

"App is not indexable by Google Search (android lint)"

// --------------------------------------------------------------------------------

Das DemonstrationActivit XML übersetzt nicht !!!

Siehe Fehlermeldung weiter unten !!!

To support older versions than API 17 (project specifies 16) you should also add android:layout_marginRight="10dp" less... (Strg+F1)
API 17 adds a textAlignment attribute to specify text alignment. However, if you are supporting older versions than API 17, you must also specify a gravity or layout_gravity attribute, since older platforms will ignore the textAlignment attribute.

// --------------------------------------------------------------------------------
