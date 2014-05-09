<?
--[[
 Copyright (C) 2000-2014 Geometria Contributors
 http://geocentral.net/geometria
 
 Geometria is free software released under the MIT License
 http://opensource.org/licenses/MIT
--]]

    read_only = false;
    write = mg and mg.write or print
    
    function getall()
        local select_stmt = db:prepare([[ SELECT id, parent_id, type, name, content FROM samples ]])
        while select_stmt:step() == sqlite3.ROW do
            local values = select_stmt:get_values()
            local id = values[1]
            local parentId = values[2] or ""
            local type = values[3]
            local name = values[4]
            write(id .. "|" .. parentId .. "|" .. type .. "|" .. name .. "\n")
        end
        write(output)
    end

    function getcontent(parameters)
        local table = parameters["table"]
        local id = parameters["id"]
        local name = parameters["name"]
        local column = id and "id" or name and "name";
        local query = "SELECT content FROM " .. table .. " WHERE " .. column .. "=?"
        local select_stmt = db:prepare(query)
        if id then
            select_stmt:bind_values(id)
        elseif name then
            select_stmt:bind_values(name)
        end
        local output = ""
        if select_stmt:step() == sqlite3.ROW then
            output = select_stmt:get_value(0) or ""
        end
        write(output)
    end

    function setcontent(parameters)
        local id = parameters["id"]
        local content = ""
        local chunk = read()
        while #chunk > 0 do
            content = content .. chunk
            chunk = read()
        end
        local update_stmt = db:prepare[[ UPDATE samples SET content=? WHERE id=? ]]
        update_stmt:bind_values(content, id)
        update_stmt:step()
        update_stmt:finalize()
        write("OK")
    end
    
    function add(parameters)
        local parentId = parameters["parent_id"]
        local type = parameters["type"]
        local name = parameters["name"]
        local content = ""
        local chunk = read()
        while #chunk > 0 do
            content = content .. chunk
            chunk = read()
        end
        local insert_stmt
        if #content > 0 then
            insert_stmt =
                db:prepare[[ INSERT INTO samples (parent_id, type, name, content) VALUES (?, ?, ?, ?) ]]
            insert_stmt:bind_values(parentId, type, name, content)
        else
            insert_stmt = db:prepare[[ INSERT INTO samples (parent_id, type, name) VALUES (?, ?, ?) ]]
            insert_stmt:bind_values(parentId, type, name)
        end
        insert_stmt:step()
        insert_stmt:finalize()
        local select_stmt = db:prepare([[ SELECT id FROM samples WHERE parent_id=? AND name=? ]])
        select_stmt:bind_values(parentId, name)
        select_stmt:step()
        local value = select_stmt:get_value(0)
        write(value)
    end

    function setparent(parameters)
        local id = parameters["id"]
        local parentId = parameters["parent_id"]
        local update_stmt = db:prepare[[ UPDATE samples SET parent_id=? WHERE id=? ]]
        update_stmt:bind_values(parentId, id)
        update_stmt:step()
        update_stmt:finalize()
        local select_stmt = db:prepare([[ SELECT parent_id FROM samples WHERE id=? ]])
        select_stmt:bind_values(id)
        select_stmt:step()
        local value = select_stmt:get_value(0)
        local result = tostring(value) == parentId and "OK" or ""
        write(result)
    end
    
    function rename(parameters)
        local id = parameters["id"]
        local name = parameters["name"]
        local update_stmt = db:prepare[[ UPDATE samples SET name=? WHERE id=? ]]
        update_stmt:bind_values(name, id)
        update_stmt:step()
        update_stmt:finalize()
        local select_stmt = db:prepare[[ SELECT name from samples WHERE id=? ]]
        select_stmt:bind_values(id)
        select_stmt:step()
        local value = select_stmt:get_value(0)
        local result = value == name and "OK" or ""
        write(result)
    end

    function delete_recursive(id)
        local select_stmt = db:prepare[[ SELECT id from samples WHERE parent_id=? ]]
        select_stmt:bind_values(id)
        while select_stmt:step() == sqlite3.ROW do
            local childId = select_stmt:get_values()[1]
            delete_recursive(childId)
        end
        local delete_stmt = db:prepare[[ DELETE FROM samples WHERE id=? ]]
        delete_stmt:bind_values(id)
        delete_stmt:step()
        delete_stmt:finalize()
    end
    
    function delete(parameters)
        local id = parameters["id"]
        delete_recursive(id)
        write("OK")
    end
    
    function isreadonly()
        write(read_only and "TRUE" or "FALSE")
    end
    
    function urldecode(str)
        str = string.gsub(str, "+", " ")
        str = string.gsub(str, "%%(%x%x)", function(h)
            return string.char(tonumber(h, 16))
        end)
        return str
    end
    
    function get_parameters()
        local request = mg and mg.request_info or request_info
        local queryString = request.query_string
        local beginParam = 1
        local endParam = 1
        local parameters = {}
        while endParam do
            endParam = string.find(queryString, "&", beginParam)
            local parameter
            if endParam == nil then
                parameter = string.sub(queryString, beginParam)
            else
                parameter = string.sub(queryString, beginParam, endParam - 1)
                beginParam = endParam + 1
            end
            local divider = string.find(parameter, "=")
            local parameterName = string.sub(parameter, 1, divider - 1)
            local parameterValue = string.sub(parameter, divider + 1)
            parameterValue = urldecode(parameterValue)
            parameters[parameterName] = parameterValue
        end 
        return parameters
    end

    function error()
        write("ERROR")
    end
    
    function doRequest()
        local parameters = get_parameters()
        local command = parameters["command"]
        local lang = parameters["lang"] or "en"
        local fileName = "sqlite/geometria-" .. lang .. ".sqlite"
        db = sqlite3.open(fileName)
        if (command == "add" and not read_only) then
            add(parameters)
        elseif command == "getall" then
            getall()
        elseif command == "getcontent" then
            getcontent(parameters)
        elseif (command == "setcontent" and not read_only) then
            setcontent(parameters)
        elseif (command == "setparent" and not read_only) then
            setparent(parameters)
        elseif (command == "rename" and not read_only) then
            rename(parameters)
        elseif (command == "delete" and not read_only) then
            delete(parameters)
        elseif (command == "isreadonly") then
            isreadonly()
        else
            error();
        end
        db:close()
    end

    doRequest()
?>
