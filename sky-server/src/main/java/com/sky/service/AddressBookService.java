package com.sky.service;

import com.sky.entity.AddressBook;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface AddressBookService {
    /**
     * 根据条件动态查询地址簿
     * @param addressBook
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 新增地址
     * @param addressBook
     */
    void addAddress(AddressBook addressBook);

    /**
     * 根据id修改地址
     * @param addressBook
     */
    void updateAddress(AddressBook addressBook);

    /**
     * 根据id删除地址
     * @param id
     */
    void deleteAddressById(Long id);

    /**
     * 设置默认地址
     * @param addressBook
     */
    void changeDefaultAddress(AddressBook addressBook);
}
