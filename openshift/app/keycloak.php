<?php
header('Content-Type: application/json');

$config = array(
    'realm' => 'master',
    'auth-server-url' => $_ENV["KEYCLOAK_URL"],
    'resource' => "app"
);

echo json_encode($config, JSON_UNESCAPED_SLASHES);
?>


