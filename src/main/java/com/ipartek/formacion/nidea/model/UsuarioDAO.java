package com.ipartek.formacion.nidea.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ipartek.formacion.nidea.pojo.Rol;
import com.ipartek.formacion.nidea.pojo.Usuario;
import com.ipartek.formacion.nidea.util.Utilidades;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class UsuarioDAO implements Persistible<Usuario> {

	private static UsuarioDAO INSTANCE = null;

	public UsuarioDAO() {
	}

	public synchronized static void createInstance() {
		if (INSTANCE == null) {
			INSTANCE = new UsuarioDAO();
		}
	}

	public static UsuarioDAO getInstance() {
		if (INSTANCE == null) {
			createInstance();
		}
		return INSTANCE;
	}

	/**
	 * Lista de usuarios SOLO con id y nombre, usar solo para la API REST
	 * 
	 * @param nombre
	 *            String con el nombre a buscar
	 * @return
	 */
	public List<Usuario> getAllApiByName(String nombre) {
		ArrayList<Usuario> lista = new ArrayList<Usuario>();
		String sql = "SELECT id , nombre FROM usuario  WHERE nombre LIKE ? ORDER BY nombre ASC LIMIT 25;";
		try (Connection con = ConnectionManager.getConnection(); PreparedStatement pst = con.prepareStatement(sql);) {

			pst.setString(1, "%" + nombre + "%");
			try (ResultSet rs = pst.executeQuery()) {

				Usuario usuario = null;
				while (rs.next()) {
					usuario = new Usuario();
					usuario.setNombre(rs.getString("nombre"));
					usuario.setId(rs.getInt("id"));
					lista.add(usuario);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return lista;
	}

	public List<Usuario> checkName(String nombre) {
		ArrayList<Usuario> lista = new ArrayList<Usuario>();
		String sql = "SELECT id , nombre FROM usuario  WHERE nombre = ? ORDER BY nombre ASC LIMIT 25;";
		try (Connection con = ConnectionManager.getConnection(); PreparedStatement pst = con.prepareStatement(sql);) {

			pst.setString(1,  nombre );
			try (ResultSet rs = pst.executeQuery()) {

				Usuario usuario = null;
				while (rs.next()) {
					usuario = new Usuario();
					usuario.setNombre(rs.getString("nombre"));
					usuario.setId(rs.getInt("id"));
					lista.add(usuario);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return lista;
	}
	
	
	public ArrayList<Usuario> getByName(String search) {
		ArrayList<Usuario> lista = new ArrayList<Usuario>();
		String sql = "SELECT u.id as id_usuario, u.nombre as nombre_usuario, u.password, r.id as id_rol, r.nombre as nombre_rol FROM usuario AS u, rol as r WHERE u.id_rol = r.id AND u.nombre LIKE ? ORDER BY u.id DESC LIMIT 500;";
				
		try (Connection con = ConnectionManager.getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, "%" + search + "%");
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					lista.add(mapper(rs));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
}
	
	
	
	
	
	/**
	 * Buscamos un usuario por nombre y password
	 * 
	 * @param nombre
	 *            String nombre del Usuario
	 * @param pass
	 *            String password del Usuario
	 * @return Usuario si existe, null si no lo encuentra
	 */
	public Usuario check(String nombre, String pass) {
		Usuario resul = null;
		String sql = "SELECT u.id as 'id_usuario', u.nombre as 'nombre_usuario', u.password as 'password', r.id as 'id_rol', r.nombre as 'nombre_rol' "
				+ "FROM usuario as u, rol as r " + "WHERE u.id_rol = r.id AND u.nombre=? and u.password = ?;";
		try (Connection con = ConnectionManager.getConnection(); PreparedStatement pst = con.prepareStatement(sql);) {
			pst.setString(1, nombre);
			pst.setString(2, pass);
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					resul = mapper(rs);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul;
	}

	@Override
	public ArrayList<Usuario> getAll() {

		ArrayList<Usuario> lista = new ArrayList<Usuario>();
		String sql = "SELECT u.id as id_usuario, u.nombre as nombre_usuario, u.password, r.id as id_rol, r.nombre as nombre_rol FROM usuario AS u, rol as r WHERE u.id_rol = r.id ORDER BY u.id DESC LIMIT 500;";

		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql);
				ResultSet rs = pst.executeQuery()) {

			while (rs.next()) {
				lista.add(mapper(rs));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return lista;
}

	@Override
	public Usuario getById(int id) {
		Usuario usuario = new Usuario();
		String sql = "SELECT u.id as 'id_usuario', u.nombre as 'nombre_usuario', u.password, r.id as 'id_rol', r.nombre as 'nombre_rol' FROM usuario as u, rol as r  WHERE u.id_rol = r.id AND u.id = ?;";
		try (Connection con = ConnectionManager.getConnection(); PreparedStatement pst = con.prepareStatement(sql);) {

			pst.setInt(1, id);
			try (ResultSet rs = pst.executeQuery()) {

				while (rs.next()) {
					usuario = mapper(rs);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return usuario;
	}

	@Override
	public boolean save(Usuario pojo) {
		boolean resul = false;

		// sanear el nombre
		pojo.setNombre(Utilidades.limpiarEspacios(pojo.getNombre()));

		if (pojo != null) {
			if (pojo.getId() == -1) {
				resul = crear(pojo);
			} else {
				resul = modificar(pojo);
			}
		}

		return resul;
	}

	public boolean modificar(Usuario pojo) {
		boolean resul = false;
		String sql = "UPDATE usuario SET nombre=? as 'nombre_usuario', password=? as 'password', id_rol=? WHERE  id=?;";
		try (Connection con = ConnectionManager.getConnection(); PreparedStatement pst = con.prepareStatement(sql);) {

			pst.setString(1, pojo.getNombre());
			pst.setString(2, pojo.getPass());
			pst.setInt(3, pojo.getRol().getId());
			pst.setInt(4, pojo.getId());

			int affectedRows = pst.executeUpdate();
			if (affectedRows == 1) {
				resul = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul;
	}

	public boolean crear(Usuario pojo) {
		boolean resul = false;
		String sql = "INSERT INTO  usuario (nombre, password,id_rol,email) VALUES ( ?,?, ?, ? );";

		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);) {

			pst.setString(1, pojo.getNombre());
			pst.setString(2, pojo.getPass());
			pst.setInt(3, 2);
			pst.setString(4, pojo.getEmail());
			

			int affectedRows = pst.executeUpdate();
			if (affectedRows == 1) {
				// recuperar ID generado de forma automatica
				try (ResultSet rs = pst.getGeneratedKeys()) {
					while (rs.next()) {
						pojo.setId(rs.getInt(1));
						resul = true;
					}
				}
			}
		}catch (MySQLIntegrityConstraintViolationException e) {
			e.printStackTrace();
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
		return resul;

	}

	@Override
	public boolean delete(int id) {
		boolean resul = false;
		String sql = "DELETE FROM `usuario` WHERE  `id`= ?;";
		try (Connection con = ConnectionManager.getConnection(); PreparedStatement pst = con.prepareStatement(sql);) {
			pst.setInt(1, id);
			int affectedRows = pst.executeUpdate();
			if (affectedRows == 1) {
				resul = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul;
	}

	@Override
	public Usuario mapper(ResultSet rs) throws SQLException {
		Usuario u = new Usuario();
		u.setId(rs.getInt("id_usuario"));
		u.setNombre(rs.getString("nombre_usuario"));
		u.setPass(rs.getString("password"));

		// Rol del usuario
		Rol rol = new Rol();
		rol.setId(rs.getInt("id_rol"));
		rol.setNombre(rs.getString("nombre_rol"));
		u.setRol(rol);

		return u;
	}
}


