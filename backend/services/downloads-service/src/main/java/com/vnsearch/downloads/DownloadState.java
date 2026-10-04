package com.vnsearch.downloads;

/**
 * Trang thai mot luot tai xuong.
 *
 * <p>Cac gia tri phai KHOP CHINH XAC voi rang buoc {@code ck_downloads_state}
 * trong V1__so_tai_xuong.sql. Them mot gia tri o day ma quen them vao rang
 * buoc thi moi phep ghi voi gia tri do se bi CSDL tu choi — mot loi lo ra
 * ngay, va do la huong dung: hong to con hon luu mot trang thai ma khong ai
 * biet nghia la gi.
 */
public enum DownloadState {

    /** Dang tai. Duy nhat trang thai nay va PAUSED cho phep cap nhat tien do. */
    IN_PROGRESS,

    /** Nguoi dung tam dung. Van con the tiep tuc. */
    PAUSED,

    COMPLETED,

    /** Nguoi dung huy. Khac INTERRUPTED: day la lua chon, khong phai su co. */
    CANCELLED,

    /**
     * Dut giua chung vi loi mang, het dia, hoac dong ung dung.
     *
     * <p>Tach khoi CANCELLED vi giao dien doi xu khac nhau: mot luot bi gian
     * doan co nut "thu lai", mot luot bi huy thi khong. Gop chung mot trang
     * thai la mat kha nang do do — khong ai tra loi duoc "bao nhieu phan tram
     * luot tai that bai vi loi".
     */
    INTERRUPTED;

    /** Da ket thuc chua — quyet dinh viec co bat buoc {@code finished_at} hay khong. */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == INTERRUPTED;
    }

    public boolean canTransitionTo(DownloadState next) {
        if (this == next) {
            return true;
        }
        return switch (this) {
            case IN_PROGRESS -> true;                   // sang bat ky trang thai nao
            case PAUSED -> next != COMPLETED;           // phai tiep tuc truoc khi xong
            case COMPLETED, CANCELLED -> false;         // trang thai cuoi
            case INTERRUPTED -> next == IN_PROGRESS;    // chi duoc thu lai
        };
    }
}
