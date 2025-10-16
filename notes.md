docker compose up -d → start containers in background (detached mode).

docker compose down → stop and remove containers + network.

docker compose ps → see which containers are running.

docker compose logs -f → see logs in real-time.

docker compose restart <service> → restart a specific service like mysql.




for mysql - 
docker exec -it banking-mysql mysql -u bankinguser -p