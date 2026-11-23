
package ke.ac.kyu.srms.service;

import ke.ac.kyu.srms.model.Admin;
import ke.ac.kyu.srms.repository.AdminRepository;
import ke.ac.kyu.srms.repository.AdminRepository.AdminAccount;
import ke.ac.kyu.srms.security.AdminPasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AdminAuthService {

    private final AdminRepository adminRepository;

    public AdminAuthService() {
        this.adminRepository = new AdminRepository();
    }

    /**
     * Authenticates an administrator.
     *
     * Returns the authenticated administrator when successful.
     * Returns Optional.empty() when credentials are invalid
     * or the account is inactive.
     */
    public Optional<Admin> authenticate(
            String username,
            char[] password
    ) throws SQLException {

        if (username == null
                || username.isBlank()
                || password == null
                || password.length == 0) {
            return Optional.empty();
        }

        Optional<AdminAccount> accountOptional =
                adminRepository.findByUsername(username);

        if (accountOptional.isEmpty()) {
            return Optional.empty();
        }

        AdminAccount account = accountOptional.get();
        Admin admin = account.admin();

        if (!admin.isActive()) {
            return Optional.empty();
        }

        boolean passwordMatches = AdminPasswordUtil.verifyPassword(
                password,
                account.passwordHash()
        );

        if (!passwordMatches) {
            return Optional.empty();
        }

        boolean updated = adminRepository.updateLastLogin(admin.getId());

        if (!updated) {
            return Optional.empty();
        }

        return Optional.of(admin);
    }
}
