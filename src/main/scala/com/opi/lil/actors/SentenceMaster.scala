package com.opi.lil.actors

import akka.actor.Status.Status
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.datastax.driver.core.Cluster
import com.opi.lil.actors.CheckerProtocol.{CheckMe, PleaseLetMeIn}
import com.opi.lil.core.DatabaseHandler
import com.opi.lil.core.GenericRouter._

import scala.io.Source

/**
*  Created by roziewski on 2015-04-01.
*/
class SentenceMaster(dbaseHandler: Cluster) extends Actor with ActorLogging{
  import WebsiteProtocol._

  val prefix = "https://aws-publicdatasets.s3.amazonaws.com/"
  val threshold = 1

  val databaseHandler = new DatabaseHandler(dbaseHandler)
  private val checkerRouter = makeRouter[Checker, CheckMe]("CheckerRouter", context)

//  private val bouncerRouter = makeRouter[Bouncer, PleaseLetMeIn]("BouncerRouter", context)

  private[this] var urls: List[String] = Nil
  private[this] var urlStatues: List[(String, Status)] = Nil

  var totalTime: Long = 0

  def receive = {

    case StartIteratingOverWebsites(numActors) =>
      log.info(s"${self} message received, creating ${numActors} actors")
//      beginProcessing(databaseHandler.getWebsites, createWorkers(numActors))
//      beginProcessing("Kasiulkowe prace szydełkowe: styczeń 2013 Strony blog lista postów SKLEP o mnie kontakt czwartek, 31 stycznia 2013 Podsumowanie miesiąca ---> styczeń Dzisiaj ostatni dzień stycznia. Miesiąc uciekł szybko. Pewnie głównie dlatego, że to miesiąc, który kończy semestr i zaczyna najgorszy okres w życiu studenta, czyli sesję :) Dopiero w tym roku zdałam sobie do końca sprawę, że matematyka to ciężki kierunek! Ludzie z roku studiują na dwóch kierunkach, na tym drugim wszystko pozaliczane, a na matematyce końca nie widać :D Koniec mojego narzekania. Mam nadzieję, że kiedyś mi się to opłaci. Za to chciałabym Wam bardzo podziękować, że jesteście. Oglądacie mojego bloga, komentujecie, chcecie tutaj być i że Wam się podoba. Ciągle jest Was coraz więcej, co mnie ogromnie cieszy! W porównaniu z sierpniem 2013 w styczniu jest Was aż trzykrotnie więcej! Bardzo Ci dziękuję mój drogi Czytelniku! Podsumowujmy... STYCZEŃ 1. Na pierwszym miejscu bezapelacyjnie znalazł się mój przepis na komin z kapturem wyprzedzając wszystkich o połowę wyświetleń. 2. Drugie miejsce zajął początkujący Łukasz, a właściwie jego stringi. 3. Trzecie miejsce przypadło w udziale pytaniu o spotkania robótkowe. 4. Kolejne miejsce ma zaszczyt zajmować kopalnia talentów z Martą i jej ozdobami ziemi (jeszcze raz zapraszam do oglądania jej sutaszu :) 5. Na zaszczytnym piątym miejscu znalazł się znowu post o spotkaniach robótkowych (przypominam, że powstał blog i czeka na Twój wpis o najlepszym miejscu na spotkanie :) 6. Ku mojemu zaskoczeniu w pierwszej dziesiątce znalazł się kolorowy komin. 7. Siódme miejsce zajął dumnie post z opisem na rękawiczkę z ciasteczkowym potworem. 8. Na ósmym miejscu znalazło się pytanie o szczęście. 9. Nie przestają Wam sie podobać moje opisy :) Na przedostatnim miejscu w TOP 10 znalazł się przepis na mini SpongeBoba. 10. Na zaszczytnym ostatnim miejscu z liczbą 100 zdobytych od Was punktów (wyświetleń) dumnie stoi 7200 oczek, czyli tęczowy szalik. A jaki jest Twój ulubiony post spośród tych dziesięciu? Etykiety: podsumowanie 9 komentarzy: Wyślij pocztą e-mailWrzuć na blogaUdostępnij w usłudze TwitterUdostępnij w usłudze FacebookUdostępnij w serwisie Pinterest Czwartkowe porady kasiulkowe ---> przyszywanie aplikacji Dzisiaj marnie, ale mam nadzieję, że Wam się spodoba mój pomysł na poradę dla Was... Jak przyszyć aplikację do Twojej pracy? Miałam problem z wyszywaniem czy przyszywaniem aplikacji na przestrzennych pracach, bo się okazywało, że niechcący zszyłam np. oba boki rękawiczki dla zakochanych przy przyszywaniu łosia. Oczywiście, że można podłożyć rękę, ale przy rękawiczkach nie jest to takie łatwe w każdym momencie szycia. Dlatego też Kasia wkłada do środka kawałek kartki Przypinam szpilkami aplikacje do przyszycia sprawdzając, aby wszystko było równo. Potem biorę igłę i przyszywam włóczką, z której był robiony łosiek. A potem to już łosiek jest gotowy :) Trzeba tylko wyjąć kartkę. Jutro przedstawię Wam bliżej tego łosia, bo wiąże się z nim dosyć ciekawa (a już na pewno napawająca mnie dumą) historia. Etykiety: mój film / opis / schemat, porady szydełkowe 6 komentarzy: Wyślij pocztą e-mailWrzuć na blogaUdostępnij w usłudze TwitterUdostępnij w usłudze FacebookUdostępnij w serwisie Pinterest środa, 30 stycznia 2013 Inspirujący post po raz drugi Nadal mało się u mnie dzieję, więc zapraszam po inspiracje (niektóre źródła prowadzą do opisów, co jest zaznaczone w opisie): Zdjęcia dzieci w czapkach (uwielbiam): Źródło (ravelry) Źródło (the dainty daisy - opis) Źródło (ETSY) Źródło (ETSY) Szyszki: Źródło (PlanetJune by June Gilbank) Pieski w skarpetach: Źródło (telegraph). Kwiatek z zawleczek od puszek: Źródło (ArtFire) Inspiracje Walentynkowe: Źródło (ETSY) Źródło (realsimple) Źródło (Crochet Spot - opis) Źródło (i heart hand i crafts - opis) Źródło (visual heart) Fascynujące wzory: Źródło (charami - opis) Źródło (domihobby - opis) Źródło (Dearest Debi) Źródło (simply crochet - opis) Źródło (flickr) Maskotki i zabawki: Źródło (En Guzel Orgulerim) Źródło (En Guzel Orgulerim) Źródło (live internet) Źródło (sulia) Zapraszam do odwiedzenia mojego bloga o Spotkaniach Robótkowych :) W gronie obserwatorów witam: iska_k oraz My Crochet Privacy Dziękuję Wam, że jesteście :) Co z inspiracji spodobało Ci się najbardziej? Etykiety: inspiracje, opis / schemat 15 komentarzy: Wyślij pocztą e-mailWrzuć na blogaUdostępnij w usłudze TwitterUdostępnij w usłudze FacebookUdostępnij w serwisie Pinterest wtorek, 29 stycznia 2013 Spotkania robótkowe (o blogu) Mówiłam, planowałam, pytałam... i powstaje po woli :) Moje nowe wirtualne dziecko: spotkania robótkowe w całej Polsce W skrócie chodzi o to, aby wyjść z domów i robić ręcznie w większych grupach, dzielić się pasjami itd... Proszę Was bardzo o umieszczania banerka na swoich blogach, aby wieści szybko się rozeszły i żebyście jak najszybciej znaleźli osoby chętne do spotkań w Waszej okolicy. Na blogu pojawiły się informacje o kilku spotkaniach. Proszę osoby bardziej zorientowane ode mnie o poprawienie ewentualnych informacji lub ich uzupełnienie. Oczywiście zbieram też informacje o innych spotkaniach. Proszę o wszelkie sugestie i poprawki : ) robotkowe@gmail.com Etykiety: inne strony 16 komentarzy: Wyślij pocztą e-mailWrzuć na blogaUdostępnij w usłudze TwitterUdostępnij w usłudze FacebookUdostępnij w serwisie Pinterest poniedziałek, 28 stycznia 2013 Inspirujący post Blog jest w trakcie tworzenia :) Bardzo dziękuję Wam za podpowiedzi. Czekam na kolejne pomysły. Opowiedziałam na część Waszych wypowiedzi pod wczorajszym pytaniem :) Sesja w toku. Mam niewiele czasu do pracy. Ubolewam, ale jak się postaram, to szybko będę miała to za sobą. Nie mam czasy na zbyt wiele, więc dzisiaj tylko inspiracje, które mnie zachwyciły. Śliczne wzory: Źródło. Źródło. Buciki: Źródło. Źródło. Źródło. Przecudowne etui na szydełka: Źródło. Kaktusiki: Źródło. Torebka (już chcę podobną :) Źródło. Miś: Źródło. Motto na dziś: Źródło. Wśród moich stałych bywalców witam: Plecionki Ineski, Ilonę Szczęsną, Ulkę M, alicja204, igielka.mb, Niebieską Kokardkę. Bardzo dziękuję za to, że jesteście. Etykiety: inspiracje 17 komentarzy: Wyślij pocztą e-mailWrzuć na blogaUdostępnij w usłudze TwitterUdostępnij w usłudze FacebookUdostępnij w serwisie Pinterest niedziela, 27 stycznia 2013 Pytanie na śniadanie ---> spotkania robótkowe c.d. Witam Was w ten niedzielny poranek! Jak wiadomo, tydzień temu pytałam Was o spotkania robótkowe. Pytałam, bo to było dla mnie wtedy \"na czasie\" - wybierałam się na spotkanie w amiQsie i byłam tym bardzo podekscytowana. :) Dzięki Waszym komentarzom stworzyłam listę z liczbą osób, które na pewno tworzą i chcą się spotkać w danych miastach: Żywiec Kluczbork Kędzierzyn-Koźle Warszawa Tarnów (2 osoby) Kraków (Małopolanki się mało odzywały :) Bydgoszcz (3 osoby) Katowice Lublin Gdańsk (3 osoby) Słupsk Zamość (2 osoby :) lubelskie (2 osoby) Jelenia Góra Koszalin Łódź Miechów W związku z tak dużą liczbą osób zainteresowanych spotkaniami i tak wielkimi niedoborem informacji na ich temat pomyślałam o stworzeniu bloga: Spotkania robótkowe z całej Polski. Chciałabym, aby to był zbiór informacji o osobach chętnych do spotkań w danym mieście (wtedy można by było zorganizować spotkanie) oraz o spotkaniach, które już się odbywają. Co sądzisz o takim blogu? Jakieś inne informacje powinny się znaleźć na tym blogu? Etykiety: pytanie na śniadanie 25 komentarzy: Wyślij pocztą e-mailWrzuć na blogaUdostępnij w usłudze TwitterUdostępnij w usłudze FacebookUdostępnij w serwisie Pinterest sobota, 26 stycznia 2013 Shrekowa rękawiczka Próbowałam zrobić zdjęcie rękawiczce, ale wyszło mi takie dziwne coś: Ogr, jako strasznie ciekawe stworzenie, chciał koniecznie zobaczyć, co takiego trzymam w rękach. Na szczęście udało mi się go przekonać do zajęcia odpowiedniego miejsca. Dalej już był całkiem spokojny i zdyscyplinowany. Rękawiczka jest prezentem urodzinowym dla mojej współlokatorki, która dzisiaj ma urodziny :) Etykiety: moje prace szydełkowe, rękawiczki dla zakochanych i inne 17 komentarzy: Wyślij pocztą e-mailWrzuć na blogaUdostępnij w usłudze TwitterUdostępnij w usłudze FacebookUdostępnij w serwisie Pinterest Nowsze posty Starsze posty Strona główna Subskrybuj: Posty (Atom) Zobacz także: Szukaj w tym blogu Ładowanie... Witaj! Jestem Kasia i pokażę Ci moje prace, kawałeczek mojego życia oraz szydełkowe schematy, porady i inspiracje. Zapraszam :) Najciekawsze posty Czwartkowe porady kasiulkowe ---> jak szukać wzorów szydełkowych? (13 sposobów) Czwartkowe porady kasiulkowe ---> Jak sprzedawać rękodzieło bez działalności? Czwartkowe porady kasiulkowe ---> jak czytać schematy? cz. 1 (aż 8 przykładów) Inspirujące poniedziałki ---> tuniki i sukienki dla małych dam Inspirujące poniedziałki ---> 9 szydełkowych koszy Inspirujące poniedziałki", createWorkers(numActors))
      beginProcessing("Jestem Kasia i pokażę Ci moje prace, kawałeczek mojego życia oraz szydełkowe schematy, porady i inspiracje.", createWorkers(numActors))

    case ProcessingFinished(url, data: Data) =>

  }

  private[this] def createWorkers(numActors: Int) = {
    for (i <- 0 until numActors) yield context.actorOf(Props(new SentenceWorker(databaseHandler, checkerRouter)), name = s"Sentence-Worker-${i}")
  }

  private[this] def getURLs(src: String): List[String] = {
    Source.fromFile(src)
      .getLines()
      .map(line => prefix + line)
      .toList
  }


  private[this] def beginProcessing(websites: Iterable[Website], workers: Seq[ActorRef]) {
    websites.zipWithIndex.foreach( e => {
//      workers(e._2 % workers.size) ! ProcessWebsite(e._1)
    })

  }

  private[this] def beginProcessing(websites: String, workers: Seq[ActorRef]) {
    Array(websites).zipWithIndex.foreach( e => {
      workers(e._2 % workers.size) ! ProcessWebsite(e._1)
    })

  }

  private[this] def isFinished = {
    log.info(s"Size of urlStatuses: ${urlStatues.size} == urls.size: ${urls.size} -> Stop")
//    (urlStatues.size >= threshold) || (urlStatues.size == urls.size)
    (urlStatues.size == urls.size)
  }

  private[this] def shutdown() {
    log.info(s"Number of files to process: ${urls.size}")
    log.info(s"Number of files processed: ${urlStatues.size}")
    log.info(s"System context shutdown: ${self}")
    context.system.shutdown()
  }

  override def postStop(): Unit = {
    log.info(s"Master actor is stopped: ${self}")
  }
}



