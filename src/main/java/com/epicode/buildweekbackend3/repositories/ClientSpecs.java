package com.epicode.buildweekbackend3.repositories;

import com.epicode.buildweekbackend3.entities.Client;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// Raccolta di "pezzi di WHERE" per la ricerca dei Clienti. Ogni metodo restituisce
// una Specification<Client>: un oggetto che Spring Data traduce in una condizione SQL.
// Qui vive la sintassi verbosa della Criteria API (root, cb, ...), così il service
// resta leggibile.
//
// Nel lambda (root, query, cb) -> ... :
//   root  = "la tabella Client"; root.get("x") = il campo x dell'entity
//   cb    = CriteriaBuilder, la fabbrica di condizioni (like, >=, <=, ...)
//   query = la query intera, qui non serve
// root.<Tipo>get("campo") = "type witness": dice al compilatore di che tipo è il campo,
// altrimenti i confronti >= / <= non compilano (Object non è Comparable).
public final class ClientSpecs {

    // costruttore privato: è una classe di soli metodi statici, non va istanziata
    private ClientSpecs() {
    }

    // nome azienda che contiene "value", senza distinzione maiuscole/minuscole:
    // LOWER(company_name) LIKE '%value%'
    public static Specification<Client> nameContains(String value) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.<String>get("companyName")), "%" + value.toLowerCase() + "%");
    }

    // fatturato annuo >= value
    public static Specification<Client> revenueAtLeast(BigDecimal value) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<BigDecimal>get("annualRevenue"), value);
    }

    // fatturato annuo <= value
    public static Specification<Client> revenueAtMost(BigDecimal value) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<BigDecimal>get("annualRevenue"), value);
    }

    // creato dal giorno "day" in poi. createdAt è un LocalDateTime, quindi confronto
    // contro le 00:00 di quel giorno per includerlo tutto.
    public static Specification<Client> insertedFrom(LocalDate day) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), day.atStartOfDay());
    }

    // creato fino al giorno "day" incluso: confronto contro le 23:59:59.999 di quel
    // giorno, altrimenti un cliente creato alle 14:00 verrebbe escluso.
    public static Specification<Client> insertedTo(LocalDate day) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("createdAt"), day.atTime(LocalTime.MAX));
    }

    // ultimo contatto >= day. lastContactDate è gia' un LocalDate, confronto diretto.
    public static Specification<Client> contactedFrom(LocalDate day) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<LocalDate>get("lastContactDate"), day);
    }

    // ultimo contatto <= day
    public static Specification<Client> contactedTo(LocalDate day) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<LocalDate>get("lastContactDate"), day);
    }
}
