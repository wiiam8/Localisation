<?php

header("Content-Type: application/json; charset=utf-8");

include_once __DIR__ . '/service/PositionService.php';

try {
    $service = new PositionService();
    echo json_encode(["positions" => $service->getAll()]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["positions" => [], "error" => $e->getMessage()]);
}

?>