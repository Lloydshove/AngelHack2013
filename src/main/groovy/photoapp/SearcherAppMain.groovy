package photoapp


class SearcherApp {

    def searchFor(Date startDate, Date endDate) {
        println """searching for $startDate to $endDate"""
    }
}

String.metaClass.asDate = { Date.parse("dd-MM-yyyy", (String)delegate)}

new SearcherApp().searchFor(
        "29-08-2013".asDate(),
        "29-08-2013".asDate(),
       // new Point()
)

