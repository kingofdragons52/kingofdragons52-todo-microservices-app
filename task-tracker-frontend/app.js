const API_BASE_URL = 'http://localhost:8080';

$.ajaxSetup({
    beforeSend: function(xhr) {
        const token = localStorage.getItem('jwt_token');
        if (token) {
            xhr.setRequestHeader('Authorization', 'Bearer ' + token);
        }
    }
});

$(document).ready(function() {
    checkAuth();

    $('#login-form').on('submit', function(e) {
        e.preventDefault();
        const data = {
            email: $(this).find('input[name="email"]').val(),
            password: $(this).find('input[name="password"]').val()
        };

        $.ajax({
            url: API_BASE_URL + '/auth/login',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function(response, textStatus, request) {
                let token = request.getResponseHeader('Authorization');
                if (!token && response) token = response.token || response; 
                if (token) {
                    localStorage.setItem('jwt_token', token.replace('Bearer ', ''));
                    $('#loginModal').modal('hide');
                    $('#login-form')[0].reset();
                    checkAuth();
                }
            },
            error: function(xhr) {
                const errorMsg = xhr.responseJSON ? xhr.responseJSON.message : "Неверный логин или пароль";
                $('#login-error').text(errorMsg).removeClass('d-none');
            }
        });
    });

    $('#register-form').on('submit', function(e) {
        e.preventDefault();
        const password = $('#reg-password').val();
        const repeatPassword = $('#reg-password-repeat').val();

        if (password !== repeatPassword) {
            $('#register-error').text("Пароли не совпадают").removeClass('d-none');
            return;
        }

        const data = {
            email: $(this).find('input[name="email"]').val(),
            password: password
        };

        $.ajax({
            url: API_BASE_URL + '/user',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function(response, textStatus, request) {
                let token = request.getResponseHeader('Authorization');
                if (!token && response) token = response.token || response;
                if (token) {
                    localStorage.setItem('jwt_token', token.replace('Bearer ', ''));
                    $('#registerModal').modal('hide');
                    $('#register-form')[0].reset();
                    checkAuth();
                }
            },
            error: function(xhr) {
                const errorMsg = xhr.responseJSON ? xhr.responseJSON.message : "Ошибка регистрации";
                $('#register-error').text(errorMsg).removeClass('d-none');
            }
        });
    });

    $(document).on('click', '#btn-logout', function() {
        localStorage.removeItem('jwt_token');
        checkAuth();
    });

    $('#btn-add-task').on('click', function() {
        const title = $('#new-task-title').val().trim();
        if (!title) return;

        $.ajax({
            url: API_BASE_URL + '/tasks',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ title: title, description: '', done: false }),
            success: function() {
                $('#new-task-title').val('');
                loadTasks();
            }
        });
    });

    $(document).on('click', '.task-card', function() {
        const taskId = $(this).data('id');
        const title = $(this).find('.card-title').text();
        const desc = $(this).data('description') || '';
        const isDone = $(this).data('done');

        $('#edit-task-id').val(taskId);
        $('#edit-task-title').val(title);
        $('#edit-task-desc').val(desc);
        $('#edit-task-status').prop('checked', isDone);

        $('#taskModal').modal('show');
    });

    $('#edit-task-title, #edit-task-desc, #edit-task-status').on('blur change', function() {
        const taskId = $('#edit-task-id').val();
        if (!taskId) return;

        const data = {
            title: $('#edit-task-title').val(),
            description: $('#edit-task-desc').val(),
            done: $('#edit-task-status').is(':checked')
        };

        $('#auto-save-status').text('Сохранение...').addClass('text-warning');

        $.ajax({
            url: API_BASE_URL + '/tasks/' + taskId,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function() {
                $('#auto-save-status').text('Изменения сохранены').removeClass('text-warning').addClass('text-success');
                loadTasks(); 
            },
            error: function() {
                $('#auto-save-status').text('Ошибка сохранения!').addClass('text-danger');
            }
        });
    });

    $('#btn-delete-task').on('click', function() {
        const taskId = $('#edit-task-id').val();
        if (!taskId) return;

        if (confirm('Вы уверены, что хотите удалить эту задачу?')) {
            $.ajax({
                url: API_BASE_URL + '/tasks/' + taskId,
                type: 'DELETE',
                success: function() {
                    $('#taskModal').modal('hide');
                    loadTasks();
                }
            });
        }
    });
});


function checkAuth() {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
        showGuestZone();
        return;
    }

    $.ajax({
        url: API_BASE_URL + '/user',
        type: 'GET',
        success: function(user) {
            showUserZone(user.email);
            loadTasks();
        },
        error: function() {
            localStorage.removeItem('jwt_token');
            showGuestZone();
        }
    });
}

function showGuestZone() {
    $('#main-content').hide();
    $('#guest-content').show();
    $('#nav-auth-zone').html(`
        <button class="btn btn-outline-light me-2" data-bs-toggle="modal" data-bs-target="#loginModal">Войти</button>
        <button class="btn btn-success" data-bs-toggle="modal" data-bs-target="#registerModal">Регистрация</button>
    `);
}

function showUserZone(email) {
    $('#guest-content').hide();
    $('#main-content').show();
    $('#nav-auth-zone').html(`
        <span class="navbar-text text-white me-3 fw-bold">${email}</span>
        <button class="btn btn-danger btn-sm" id="btn-logout">Выйти</button>
    `);
}

function loadTasks() {
    $.ajax({
        url: API_BASE_URL + '/tasks',
        type: 'GET',
        success: function(tasks) {
            $('#todo-tasks-list').empty();
            $('#done-tasks-list').empty();

            tasks.forEach(task => {
                const cardHtml = `
                    <div class="card mb-2 task-card" data-id="${task.id}" data-description="${task.description || ''}" data-done="${task.done}">
                        <div class="card-body p-3">
                            <h5 class="card-title m-0 ${task.done ? 'done-task' : ''}">${task.title}</h5>
                        </div>
                    </div>
                `;

                if (task.done) {
                    $('#done-tasks-list').append(cardHtml);
                } else {
                    $('#todo-tasks-list').append(cardHtml);
                }
            });
        }
    });
}

$(document).on('click', '.toggle-password', function() {
    const targetId = $(this).data('target');
    const passwordInput = $('#' + targetId);
    
    if (passwordInput.attr('type') === 'password') {
        passwordInput.attr('type', 'text');
        $(this).text('🙈'); 
    } else {
        passwordInput.attr('type', 'password');
        $(this).text('👁️'); 
    }
});