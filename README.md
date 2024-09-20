Tentativa de implementação do desafio técnico da Compass.

Interação com MongoDB collections que seguem o mesmo modelo que o input do API por simplicidade.

O endpoint foi modificado de PUT para POST por não conter {id} na url

O endpoint foi renomeado payments no plural

Uso de LocalStack para bom funcionamento dos testes de integração que estão sem accesso a Amazon SQS.