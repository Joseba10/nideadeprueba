package com.ipartek.formacion.nidea.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.ipartek.formacion.nidea.pojo.Material;
import com.ipartek.formacion.nidea.pojo.Usuario;
import com.ipartek.formacion.nidea.util.Utilidades;
import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class MaterialDAO implements Persistible<Material> {

	private static MaterialDAO INSTANCE = null;

	// Private constructor NO se pueda hacer new y crear N instancias
	private MaterialDAO() {
	}

	// creador synchronized para protegerse de posibles problemas multi-hilo
	private synchronized static void createInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MaterialDAO();
		}
	}

	public static MaterialDAO getInstance() {
		if (INSTANCE == null) {
			createInstance();
		}
		return INSTANCE;
	}

	/**
	 * Recupera todos los materiales de la BBDD ordenados por id descendente
	 * 
	 * @return ArrayList<Material> si no existen registros new ArrayList<Material>()
	 */
	@Override
	public ArrayList<Material> getAll() {
		ArrayList<Material> lista = new ArrayList<Material>();
		String sql = "SELECT material.id, material.nombre, precio, u.id as 'id_usuario', u.nombre as 'nombre_usuario' FROM `material`,`usuario` as u WHERE material.id_usuario = u.id ORDER BY material.id DESC LIMIT 500";
		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql);
				ResultSet rs = pst.executeQuery();) {
			Material m = null;
			while (rs.next()) {
				m = mapper(rs);
				lista.add(m);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
	}

	@Override
	public Material getById(int id) {
		Material material = null;
		String sql = "SELECT material.id, material.nombre, material.precio, usuario.id as 'id_usuario', usuario.nombre as 'nombre_usuario' FROM  material, usuario WHERE material.id_usuario = usuario.id AND material.id =?; ;";
		try (Connection con = ConnectionManager.getConnection(); PreparedStatement pst = con.prepareStatement(sql);) {
			pst.setInt(1, id);
			
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					material = mapper(rs);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return material;
	}

	@Override
	public boolean save(Material pojo) throws MysqlDataTruncation, MySQLIntegrityConstraintViolationException {
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

	private boolean modificar(Material pojo) {
		boolean resul = false;
		String sql = "UPDATE material SET nombre=?, precio=?, id_usuario=? WHERE  id=?;";
		try (Connection con = ConnectionManager.getConnection(); PreparedStatement pst = con.prepareStatement(sql);) {

			pst.setString(1, pojo.getNombre());
			pst.setFloat(2, pojo.getPrecio());
			pst.setInt(3, pojo.getUsuario().getId());
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

	private boolean crear(Material pojo) throws MySQLIntegrityConstraintViolationException, MysqlDataTruncation {
		boolean resul = false;
		String sql = "INSERT INTO  material (nombre, precio,id_usuario) VALUES ( ?, ?, ? );";
	
		try (Connection con = ConnectionManager.getConnection();
				PreparedStatement pst = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);) {

			pst.setString(1, pojo.getNombre());
			pst.setFloat(2, pojo.getPrecio());
			pst.setInt(3, pojo.getUsuario().getId());

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
		} catch (MySQLIntegrityConstraintViolationException e) {
			System.out.println("Material duplicado");
			throw e;
		} catch (MysqlDataTruncation e) {
			System.out.println("Nombre muy largo");
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resul;
	}

	@Override
	public boolean delete(int id) {
		boolean resul = false;
		String sql = "DELETE FROM `material` WHERE  `id`= ?;";
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
	public Material mapper(ResultSet rs) throws SQLException {
		Material m = null;
		if (rs != null) {
			m = new Material();
			m.setId(rs.getInt("id"));
			m.setNombre(rs.getString("nombre"));
			m.setPrecio(rs.getFloat("precio"));

			Usuario u = new Usuario();
			u.setId(rs.getInt("id_usuario"));
			u.setNombre(rs.getString("nombre_usuario"));
			m.setUsuario(u);

		}
		return m;
	}

	public ArrayList<Material> search(String nombreBuscar) {
		ArrayList<Material> lista = new ArrayList<Material>();
		//TODO Poner la sentencia bien
		String sql = "SELECT m.id, m.nombre, precio, u.id as id_usuario, u.nombre as nombre_usuario FROM material as m, usuario as u WHERE u.id = m.id_usuario AND m.nombre LIKE ? ORDER BY m.id DESC LIMIT 500;;";
		try (Connection con = ConnectionManager.getConnection(); PreparedStatement pst = con.prepareStatement(sql);) {

			pst.setString(1, "%" + nombreBuscar + "%");
			try (ResultSet rs = pst.executeQuery();) {

				Material m = null;
				while (rs.next()) {
					m = mapper(rs);
					lista.add(m);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return lista;
	}
	
	public ArrayList<Material> getAllByUser(int idUsuario) {
				ArrayList<Material> lista = new ArrayList<Material>();
				String sql = "SELECT material.id, material.nombre, precio, u.id as 'id_usuario', u.nombre as 'nombre_usuario' FROM `material`,`usuario` as u WHERE material.id_usuario = u.id AND material.id_usuario = ? ORDER BY material.id DESC LIMIT 500";
				try (Connection con = ConnectionManager.getConnection();
						PreparedStatement pst = con.prepareStatement(sql);
						) {
					
						pst.setInt(1, idUsuario);
						
					try( ResultSet rs = pst.executeQuery(); ){					
							while (rs.next()) {						
							lista.add(mapper(rs));
							}
						}		
		
				} catch (Exception e) {
					e.printStackTrace();
				}
				return lista;
			}
	
	public boolean deleteByUser(int idMaterial, int idUsuario) {
				boolean resul = false;
				String sql = "DELETE FROM `material` WHERE  `id`= ? AND `id_usuario`=? ;";
				try (Connection con = ConnectionManager.getConnection(); PreparedStatement pst = con.prepareStatement(sql);) {
					pst.setInt(1, idMaterial);
					pst.setInt(2, idUsuario);
					int affectedRows = pst.executeUpdate();
					if (affectedRows == 1) {
						resul = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return resul;
			}

}
