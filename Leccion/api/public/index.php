<?php
require '../vendor/autoload.php';

use Slim\Factory\AppFactory;

$app = AppFactory::create();

// Agregar middleware para manejo de JSON
$app->addBodyParsingMiddleware();

// Configuración de la conexión a la base de datos
$db = new PDO('mysql:host=localhost;dbname=api', 'root', '');

// Middleware para establecer la conexión a la base de datos
$app->add(function ($request, $handler) use ($db) {
    $request = $request->withAttribute('db', $db);
    return $handler->handle($request);
});

// Ruta para crear un nuevo usuario
$app->post('/api/usuarios/create', function ($request, $response) {
    $db = $request->getAttribute('db');
    $data = $request->getParsedBody();

    // Verifica los datos recibidos
    error_log(print_r($data, true)); // Para depuración

    // Validaciones básicas
    if (!isset($data['nombre']) || !isset($data['email']) || !isset($data['password'])) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Datos incompletos']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    $nombre = trim($data['nombre']);
    $email = trim($data['email']);
    $password = trim($data['password']);

    // Validar formato del correo electrónico
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Correo electrónico inválido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Validar la longitud de la contraseña (por ejemplo, mínimo 8 caracteres)
    if (strlen($password) < 8) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'La contraseña debe tener al menos 8 caracteres']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Verificar si el correo electrónico ya está registrado
    $stmt = $db->prepare("SELECT COUNT(*) FROM usuarios WHERE email = ?");
    $stmt->execute([$email]);
    $count = $stmt->fetchColumn();

    if ($count > 0) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'El correo electrónico ya está registrado']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Hashear la contraseña para almacenarla de forma segura
    $hashedPassword = password_hash($password, PASSWORD_BCRYPT);

    // Insertar el nuevo usuario en la base de datos
    $stmt = $db->prepare("INSERT INTO usuarios (nombre, email, password) VALUES (?, ?, ?)");
    $result = $stmt->execute([$nombre, $email, $hashedPassword]);

    if ($result) {
        $response->getBody()->write(json_encode(['status' => 'Usuario registrado exitosamente']));
    } else {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'No se pudo guardar el usuario']));
    }
    return $response->withHeader('Content-Type', 'application/json');
});

// Ruta para obtener información de un usuario por ID
$app->get('/api/usuarios/read/id={id}', function ($request, $response, $args) {
    $db = $request->getAttribute('db');
    $id = $args['id'];

    // Validar que el ID sea un número entero
    if (!filter_var($id, FILTER_VALIDATE_INT)) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'ID no válido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Preparar y ejecutar la consulta
    $stmt = $db->prepare("SELECT * FROM usuarios WHERE id = ?");
    $stmt->execute([$id]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    // Verificar si se encontró el usuario
    if ($user) {
        $response->getBody()->write(json_encode($user));
    } else {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Usuario no encontrado']));
        return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
    }

    return $response->withHeader('Content-Type', 'application/json');
});

// Ruta para actualizar información de un usuario
$app->post('/api/usuarios/update/id={id}', function ($request, $response, $args) {
    $db = $request->getAttribute('db');
    $data = $request->getParsedBody();
    $id = $args['id'];

    // Validar que el ID sea un número entero
    if (!filter_var($id, FILTER_VALIDATE_INT)) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'ID no válido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Validar los datos recibidos
    if (!isset($data['nombre']) || !isset($data['email']) || !isset($data['password'])) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Datos incompletos']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Validar el formato del correo electrónico
    if (!filter_var($data['email'], FILTER_VALIDATE_EMAIL)) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Correo electrónico no válido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Hashear la nueva contraseña
    $hashedPassword = password_hash($data['password'], PASSWORD_BCRYPT);

    // Preparar y ejecutar la consulta de actualización
    $stmt = $db->prepare("UPDATE usuarios SET nombre = ?, email = ?, password = ? WHERE id = ?");
    $result = $stmt->execute([$data['nombre'], $data['email'], $hashedPassword, $id]);

    // Verificar si la actualización fue exitosa
    if ($result) {
        $response->getBody()->write(json_encode(['status' => 'success']));
    } else {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'No se pudo actualizar el usuario']));
        return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
    }

    return $response->withHeader('Content-Type', 'application/json');
});


// Ruta para eliminar un usuario
$app->post('/api/usuarios/delete', function ($request, $response) {
    $db = $request->getAttribute('db');
    $data = $request->getParsedBody();

    // Verificar los datos recibidos
    error_log(print_r($data, true)); // Para depuración

    // Validar que se haya proporcionado un ID
    if (empty($data['id'])) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'ID requerido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Validar que el ID sea un número entero
    $id = filter_var($data['id'], FILTER_VALIDATE_INT);
    if ($id === false) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'ID no válido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Verificar existencia del usuario
    $stmt = $db->prepare("SELECT * FROM usuarios WHERE id = ?");
    $stmt->execute([$data['id']]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($user) {
        // Eliminar usuario
        $stmt = $db->prepare("DELETE FROM usuarios WHERE id = ?");
        $result = $stmt->execute([$data['id']]);

        if ($result) {
            $response->getBody()->write(json_encode(['status' => 'Usuario eliminado exitosamente']));
        } else {
            $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'No se pudo eliminar el usuario']));
        }
    } else {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Usuario no encontrado']));
    }

    return $response->withHeader('Content-Type', 'application/json');
});

// Ruta para crear un nuevo producto
$app->post('/api/productos/create', function ($request, $response) {
    $db = $request->getAttribute('db');
    $data = $request->getParsedBody();

    // Verificar los datos recibidos
    error_log(print_r($data, true)); // Para depuración

    // Validar que se hayan proporcionado todos los campos requeridos
    if (empty($data['nombre']) || empty($data['descripcion']) || empty($data['precio']) || empty($data['id_usuario'])) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Datos incompletos']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Validar el formato del precio
    if (!is_numeric($data['precio']) || $data['precio'] <= 0) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Precio no válido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Validar que el ID de usuario sea un número entero
    if (!filter_var($data['id_usuario'], FILTER_VALIDATE_INT)) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'ID de usuario no válido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Preparar y ejecutar la consulta de inserción
    try {
        $stmt = $db->prepare("INSERT INTO productos (nombre, descripcion, precio, id_usuario) VALUES (?, ?, ?, ?)");
        $result = $stmt->execute([$data['nombre'], $data['descripcion'], $data['precio'], $data['id_usuario']]);

        if ($result) {
            $response->getBody()->write(json_encode(['status' => 'Producto registrado exitosamente']));
        } else {
            $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'No se pudo guardar el producto']));
            return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
        }
    } catch (PDOException $e) {
        error_log('Error en la base de datos: ' . $e->getMessage());
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Error en la base de datos']));
        return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
    }

    return $response->withHeader('Content-Type', 'application/json');
});

// Ruta para obtener información de un producto por ID
$app->get('/api/productos/read/id={id}', function ($request, $response, $args) {
    $db = $request->getAttribute('db');
    $id = $args['id'];

    // Validar que el ID sea un número entero
    if (!filter_var($id, FILTER_VALIDATE_INT)) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'ID no válido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Preparar y ejecutar la consulta
    $stmt = $db->prepare("SELECT * FROM productos WHERE id = ?");
    $stmt->execute([$id]);
    $product = $stmt->fetch(PDO::FETCH_ASSOC);

    // Verificar si se encontró el producto
    if ($product) {
        $response->getBody()->write(json_encode($product));
    } else {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Producto no encontrado']));
        return $response->withStatus(404)->withHeader('Content-Type', 'application/json');
    }

    return $response->withHeader('Content-Type', 'application/json');
});

// Ruta para obtener una lista de productos por ID de usuario
$app->get('/api/productos/list/id_usuario={id_usuario}', function ($request, $response, $args) {
    $db = $request->getAttribute('db');
    $id_usuario = $args['id_usuario'];

    // Validar que el ID de usuario sea un número entero
    if (!filter_var($id_usuario, FILTER_VALIDATE_INT)) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'ID de usuario no válido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Preparar y ejecutar la consulta
    try {
        $stmt = $db->prepare("SELECT * FROM productos WHERE id_usuario = ?");
        $stmt->execute([$id_usuario]);
        $products = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // Verificar si se encontraron productos
        if ($products) {
            $response->getBody()->write(json_encode($products));
        } else {
            $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'No se encontraron productos']));
        }
    } catch (PDOException $e) {
        error_log('Error en la base de datos: ' . $e->getMessage());
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Error en la base de datos']));
        return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
    }

    return $response->withHeader('Content-Type', 'application/json');
});

// Ruta para actualizar información de un usuario
$app->post('/api/productos/update/id={id}', function ($request, $response, $args) {
    $db = $request->getAttribute('db');
    $data = $request->getParsedBody();
    $id = $args['id'];

    // Validar que el ID sea un número entero
    if (!filter_var($id, FILTER_VALIDATE_INT)) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'ID no válido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Validar los datos recibidos
    if (!isset($data['nombre']) || !isset($data['descripcion']) || !isset($data['precio'])) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Datos incompletos']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Validar que el precio sea un número positivo
    if (!filter_var($data['precio'], FILTER_VALIDATE_FLOAT) || $data['precio'] <= 0) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Precio no válido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }
    // Preparar y ejecutar la consulta de actualización
    try {
        $stmt = $db->prepare("UPDATE productos SET nombre = ?, descripcion = ?, precio = ? WHERE id = ?");
        $result = $stmt->execute([$data['nombre'], $data['descripcion'], $data['precio'], $id]);

        if ($result) {
            $response->getBody()->write(json_encode(['status' => 'Producto Actualizado']));
        } else {
            $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'No se pudo actualizar el producto']));
            return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
        }
    } catch (PDOException $e) {
        error_log('Error en la base de datos: ' . $e->getMessage());
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Error en la base de datos']));
        return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
    }

    return $response->withHeader('Content-Type', 'application/json');
});


// Ruta para eliminar un usuario
$app->post('/api/productos/delete', function ($request, $response) {
    $db = $request->getAttribute('db');
    $data = $request->getParsedBody();

    // Verificar los datos recibidos
    error_log(print_r($data, true)); // Para depuración

    // Validar que se haya proporcionado un ID
    if (empty($data['id'])) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'ID requerido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Validar que el ID sea un número entero
    if (!filter_var($data['id'], FILTER_VALIDATE_INT)) {
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'ID no válido']));
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json');
    }

    // Verificar existencia del producto
    try {
        $stmt = $db->prepare("SELECT * FROM productos WHERE id = ?");
        $stmt->execute([$data['id']]);
        $product = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($product) {
            // Eliminar producto
            $stmt = $db->prepare("DELETE FROM productos WHERE id = ?");
            $result = $stmt->execute([$data['id']]);

            if ($result) {
                $response->getBody()->write(json_encode(['status' => 'Producto eliminado']));
            } else {
                $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'No se pudo eliminar el producto']));
            }
        } else {
            $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Producto no encontrado']));
        }
    } catch (PDOException $e) {
        error_log('Error en la base de datos: ' . $e->getMessage());
        $response->getBody()->write(json_encode(['status' => 'error', 'message' => 'Error en la base de datos']));
        return $response->withStatus(500)->withHeader('Content-Type', 'application/json');
    }

    return $response->withHeader('Content-Type', 'application/json');
});


$app->run();



