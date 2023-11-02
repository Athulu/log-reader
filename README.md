# encja logs

| pole encji  | typ danych |
| ------------- | ------------- |
| log_id  | INT AUTO_INCREMENT PRIMARY_KEY  |
| occur_time  | DAYTIME  |
| severity_level | VARCHAR(10)  |
| thread_name  | VARCHAR(127)  |
| message | TEXT  |

**log_id** to klucz główny inkrementujący się automatycznie przy każdym nowym rekordzie.

**occur_time** przechowuje czas pojawienia się logu. Nie widzę potrzeby rozdzielania tego pola na datę i godzinę, ponieważ typ DAYTIME zapewnia dokładność co do milisekundy czyli wystarczającą by nie utracić informacji czasowych z logu.

**severity_level** z ustawionym typem VARCHAR na maksymalnie 10 znaków powinien w zupełności wystarczyć do przechowywania severity levels, które mieszczą się do 6 znaków.

**thread_name** z ustawionym typem VARCHAR na maksymalnie 127 powinien pomieścić nazwę Thread, z którego pochodzi log.

**message** 65,535 bajtów powinno wystarczyć na przechowanie każdego komunikatu. Nawet przy kodowaniu UTF-8 pierwsze 128 znaków waży po 1 bajcie, a stanowią praktycznie wszystkie znaki z generowanych wiadomości. W pliku **zadanie_server.log** najdłuższy komunikat miał ~1500 znaków, więc ~44 krotność zapasu wydaje się bezpieczna. 
