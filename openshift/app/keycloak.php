<?php
header("Content-Type: application/json");

$config->realm = "master";
$config->auth-server-url = $_ENV["KEYCLOAK_URL"];
$config->resource = "app";

echo json_encode($config);
?>


