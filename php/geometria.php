<?php
/**
 * Copyright (C) 2000-2014 Geometria Contributors
 * http://geocentral.net/geometria
 * 
 * Geometria is free software released under the MIT License
 * http://opensource.org/licenses/MIT
 */

    $readOnly = true;

    function getall($pdo) {
        $db = new PDO($pdo);
        $query = "SELECT id, parent_id, type, name, content FROM samples";
        $result = $db->query($query);
        $rows = $result->fetchAll();
        foreach ($rows as $row) {
            echo $row["id"] . "|" . $row["parent_id"] . "|" . $row["type"] . "|" . $row["name"] . "\n";
        }
    }

    function getcontent($pdo) {
        $db = new PDO($pdo);
        $table = $_GET["table"];
        $id = $_GET["id"];
        $query = "SELECT content FROM {$table} WHERE id=:id";
        $stmt = $db->prepare($query);
        $stmt->bindValue(":id", $id);
        $stmt->execute();
        $content = $stmt->fetchColumn();
        echo $content;
    }

    function add($pdo) {
        $db = new PDO($pdo);
        $parentId = $_GET["parent_id"];
        $type = $_GET["type"];
        $name = $_GET["name"];
        $content = file_get_contents("php://input");
        $insertQuery =
            "INSERT INTO samples (parent_id, type, name, content) VALUES (:parentId, :type, :name, :content)";
        $insertStmt = $db->prepare($insertQuery);
        $insertStmt->bindValue(":parentId", $parentId);
        $insertStmt->bindValue(":type", $type);
        $insertStmt->bindValue(":name", $name);
        $insertStmt->bindValue(":content", $content);
        $insertStmt->execute();
        $selectQuery = "SELECT id FROM samples WHERE parent_id=:parentId AND name=:name";
        $selectStmt = $db->prepare($selectQuery);
        $selectStmt->bindValue(":parentId", $parentId);
        $selectStmt->bindValue(":name", $name);
        $selectStmt->execute();
        $value = $selectStmt->fetchColumn();
        echo $value;
    }

    function setcontent($pdo) {
        $db = new PDO($pdo);
        $id = $_GET["id"];
        $content = file_get_contents("php://input");
        $query = "UPDATE samples SET content=:content WHERE id=:id";
        $stmt = $db->prepare($query);
        $stmt->bindValue(":id", $id);
        $stmt->bindValue(":content", $content);
        $stmt->execute();
        echo "OK";
    }

    // 'rename' is reserved in PHP
    function renameitem($pdo) {
        $db = new PDO($pdo);
        $id = $_GET["id"];
        $name = $_GET["name"];
        $updateQuery = "UPDATE samples SET name=:name WHERE id=:id";
        $updateStmt = $db->prepare($updateQuery);
        $updateStmt->bindValue(":id", $id);
        $updateStmt->bindValue(":name", $name);
        $updateStmt->execute();
        $selectQuery = "SELECT name FROM samples WHERE id=:id";
        $selectStmt = $db->prepare($selectQuery);
        $selectStmt->bindValue(":id", $id);
        $selectStmt->execute();
        $value = $selectStmt->fetchColumn();
        echo $name == $value ? "OK" : "";
    }

    function delete_recursive($id, $pdo) {
        $db = new PDO($pdo);
        $selectQuery = "SELECT id FROM samples WHERE parent_id=:id";
        $selectStmt = $db->prepare($selectQuery);
        $selectStmt->bindValue(":id", $id);
        $selectStmt->execute();
        $rows = $selectStmt->fetchAll();
        foreach ($rows as $row) {
            $childId = $row["id"];
            delete_recursive($childId, $pdo);
        }
        $deleteQuery = "DELETE FROM samples WHERE id=:id";
        $deleteStmt = $db->prepare($deleteQuery);
        $deleteStmt->bindValue(":id", $id);
        $deleteStmt->execute();
    }
    
    function delete($pdo) {
        $id = $_GET["id"];
        delete_recursive($id, $pdo);
        echo "OK";
    }
    
    function setparent($pdo) {
        $db = new PDO($pdo);
        $id = $_GET["id"];
        $parentId = $_GET["parent_id"];
        $updateQuery = "UPDATE samples SET parent_id=:parentId WHERE id=:id";
        $updateStmt = $db->prepare($updateQuery);
        $updateStmt->bindValue(":parentId", $parentId);
        $updateStmt->bindValue(":id", $id);
        $updateStmt->execute();
        $selectQuery = "SELECT parent_id FROM samples WHERE id=:id";
        $selectStmt = $db->prepare($selectQuery);
        $selectStmt->bindValue(":id", $id);
        $selectStmt->execute();
        $value = $selectStmt->fetchColumn();
        echo $value == $parentId ? "OK" : "";
    }
    
    function isreadonly() {
        global $readOnly;
        echo $readOnly ? "true" : "false";
    }
    
    function error() {
        echo "ERROR";
    }
    
    function doRequest() {
        global $readOnly;
        $lang = $_GET["lang"];
        if (!$lang) {
            $lang = "en";
        }
        $pdo = "sqlite:sqlite" . "/geometria-" . $lang . ".sqlite";
        $command = $_GET["command"];
        switch ($command) {
        case "add":
            if (!$readOnly) {
                add($pdo);
            }
            else {
                error();
            }
            break;
        case "getall":
            getall($pdo);
            break;
        case "getcontent":
            getcontent($pdo);
            break;
        case "setcontent":
            if (!$readOnly) {
                setcontent($pdo);
            }
            else {
                error();
            }
            break;
        case "setparent":
            if (!$readOnly) {
                setparent($pdo);
            }
            else {
                error();
            }
            break;
        case "rename":
            if (!$readOnly) {
                renameitem($pdo);
            }
            else {
                error();
            }
            break;
        case "delete":
            if (!$readOnly) {
                delete($pdo);
            }
            else {
                error();
            }
            break;
        case "isreadonly":
            isreadonly();
            break;
        default:
            error();
        }
    }

    doRequest();
?>
